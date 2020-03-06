package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(LocacaoService.class)
public class LocacaoServiceTest {
	
	@InjectMocks
	@Spy
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
	public void alugarFilme () throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.doReturn(DataUtils.obterData(6, 3, 2020)).when(locacaoService).obterData();
		
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
		
		error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(4.0)));
	    error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(6, 3, 2020)), CoreMatchers.is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(7, 3, 2020)), CoreMatchers.is(true));
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
	public void naoDevolverDomingo() throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.doReturn(DataUtils.obterData(7, 3, 2020)).when(locacaoService).obterData();
		
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);
		
		assertThat(resultado.getDataRetorno(), MatchersProprios.paraSegunda());
	}
	
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

	
	
	@Test
	public void calcularValorLocacao() throws Exception {
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Class<LocacaoService> clazz = LocacaoService.class;
		Method metodo = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		metodo.setAccessible(true);
		Double valor = (Double) metodo.invoke(locacaoService, filmes);
		
		Assert.assertThat(valor, CoreMatchers.is(4.0));
	}
}
