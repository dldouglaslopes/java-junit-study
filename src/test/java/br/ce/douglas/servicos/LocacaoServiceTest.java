package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import br.ce.douglas.builders.FilmeBuilder;
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
	
	private LocacaoService locacaoService;
	private LocacaoDAO dao;
	private SPCService spc;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule 
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() {
		locacaoService = new LocacaoService();
		dao = Mockito.mock(LocacaoDAO.class);
		locacaoService.setLocacaoDAO(dao);
		spc = Mockito.mock(SPCService.class);
		locacaoService.setSPCService(spc);
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
	public void naoAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException, LocadoraException {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		java.util.List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		Mockito.when(spc.possuiNegativacao(usuario)).thenReturn(true);
		
		expectedException.expect(LocadoraException.class);
		expectedException.expectMessage("Usuario Negativado");
		
		locacaoService.alugarFilme(usuario, filmes);
	}
}
