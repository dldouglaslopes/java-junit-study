package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;

import br.ce.douglas.builders.FilmeBuilder;
import br.ce.douglas.builders.LocacaoBuilder;
import br.ce.douglas.builders.UsuarioBuilder;
import br.ce.douglas.daos.LocacaoDAO;
import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;
import br.ce.douglas.matchers.MatchersProprios;
import br.ce.douglas.utils.DataUtils;

public class LocacaoServiceTest {
	
	@InjectMocks
	private LocacaoService locacaoService;
	
	@Mock
	private LocacaoDAO dao;
	
	@Mock
	private SPCService spc;
	
	@Mock
	private EmailService emailService;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule 
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void teste () throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		
		Locacao locacao = locacaoService.alugarFilme(usuario, Arrays.asList(FilmeBuilder.umFilme().agora()));
		
		//error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(15.0)));
	    //error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), CoreMatchers.is(true));
	    //error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), CoreMatchers.is(true));
		
		error.checkThat(locacao.getDataLocacao(), MatchersProprios.hoje());
		error.checkThat(locacao.getDataRetorno(), MatchersProprios.hojeComDiferencaDias(1));
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void filmeSemEstoque() throws Exception{
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		
		locacaoService.alugarFilme(usuario, Arrays.asList(FilmeBuilder.umFilme().semEstoque().agora()));
	}
	
	@Test
	public void usuarioVazio() throws FilmeSemEstoqueException{
		
		try {
			locacaoService.alugarFilme(null, Arrays.asList(FilmeBuilder.umFilme().agora()));
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), CoreMatchers.is("Usuario vazio"));
		}
		
	}
	
	@Test
	public void filmeVazio() throws FilmeSemEstoqueException, LocadoraException {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		
		expectedException.expect(LocadoraException.class);
		expectedException.expectMessage("Filme vazio");
		
		locacaoService.alugarFilme(usuario, null);	
	}
	
	@Test
	public void naoDevolverDomingo() throws FilmeSemEstoqueException, LocadoraException {
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		
		Locacao resultado = locacaoService.alugarFilme(usuario, Arrays.asList(FilmeBuilder.umFilme().agora()));
		
		//boolean isSegunda = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
		//assertTrue(isSegunda);
		
		//assertThat(resultado.getDataRetorno(), new DiaSemanaMatcher(Calendar.SUNDAY));
		assertThat(resultado.getDataRetorno(), MatchersProprios.paraDia(Calendar.MONDAY));
		assertThat(resultado.getDataRetorno(), MatchersProprios.paraSegunda());
	}
	
//	public static void main(String[] args) {
//		new BuilderMaster().gerarCodigoClasse(Locacao.class);
//	}
	
	@Test
	public void naoAlugarFilmeParaNegativadoSPC() throws Exception{
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		
		java.util.List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.when(spc.possuiNegativacao(usuario)).thenReturn(true);
		
		try {
			locacaoService.alugarFilme(usuario, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			Assert.assertThat(e.getMessage(), CoreMatchers.is("Usuario Negativado"));
		}
		
		Mockito.verify(spc).possuiNegativacao(usuario);
	}
	
	@Test
	public void enviarEmailLocacoesAtradasas() {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Usuario 2").agora();
		Usuario usuario3 = UsuarioBuilder.umUsuario().comNome("Usuario atrasado").agora();

		List<Locacao> locacoes = Arrays.asList(LocacaoBuilder.umLocacao()
															.comUsuario(usuario)
															.atrasada()
															.agora(),
												LocacaoBuilder.umLocacao().
																comUsuario(usuario2)
																.agora(),
												LocacaoBuilder.umLocacao()
																.comUsuario(usuario3)
																.atrasada()
																.agora(),
												LocacaoBuilder.umLocacao()
																.comUsuario(usuario3)
																.atrasada()
																.agora());
		
		Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
		
		locacaoService.notificarAtrasos();
		
		Mockito.verify(emailService, Mockito.times(3)).notificaAtraso(Mockito.any(Usuario.class));
		
		Mockito.verify(emailService).notificaAtraso(usuario);
		Mockito.verify(emailService, Mockito.times(2)).notificaAtraso(usuario3);
		Mockito.verify(emailService, Mockito.never()).notificaAtraso(usuario2);
		Mockito.verifyNoMoreInteractions(emailService);
		Mockito.verifyZeroInteractions(spc);
	}
	
	@Test
	public void tratarErrosSPC() throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));
		
		expectedException.expect(LocadoraException.class);
		expectedException.expectMessage("Problemas com SPC, tente novamente");
		
		locacaoService.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void prorogarLocacao() {
		Locacao locacao = LocacaoBuilder.umLocacao().agora();
		
		locacaoService.prorrogarLocacao(locacao, 3);
		
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		
		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), CoreMatchers.is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.hoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.hojeComDiferencaDias(3));
	}
}
