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

import br.ce.douglas.daos.LocacaoDAO;
import br.ce.douglas.daos.LocacaoDAO2;
import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;
import br.ce.douglas.matchers.MatchersProprios;
import br.ce.douglas.utils.DataUtils;

public class LocacaoServiceTest {
	
	private LocacaoService locacaoService;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule 
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() {
		locacaoService = new LocacaoService();
		LocacaoDAO dao = new LocacaoDAO2();
		locacaoService.setLocacaoDAO(dao);
	}
	
	@Test
	public void teste () throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 3, 5.00);
		Usuario usuario = new Usuario("Douglas");
		
		Locacao locacao = locacaoService.alugarFilme(usuario, Arrays.asList(filme, filme2));
		
		//error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(15.0)));
	    //error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), CoreMatchers.is(true));
	    //error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), CoreMatchers.is(true));
		
		error.checkThat(locacao.getDataLocacao(), MatchersProprios.hoje());
		error.checkThat(locacao.getDataRetorno(), MatchersProprios.hojeComDiferencaDias(1));
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void filmeSemEstoque() throws Exception{
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 0, 5.00);
		Usuario usuario = new Usuario("Douglas");
		
		locacaoService.alugarFilme(usuario, Arrays.asList(filme, filme2));
	}
	
	@Test
	public void usuarioVazio() throws FilmeSemEstoqueException{
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 3, 5.00);
		
		try {
			locacaoService.alugarFilme(null, Arrays.asList(filme, filme2));
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), CoreMatchers.is("Usuario vazio"));
		}
		
	}
	
	@Test
	public void filmeVazio() throws FilmeSemEstoqueException, LocadoraException {
		Usuario usuario = new Usuario("Douglas");
		
		expectedException.expect(LocadoraException.class);
		expectedException.expectMessage("Filme vazio");
		
		locacaoService.alugarFilme(usuario, null);	
	}
	
	@Test
	public void naoDevolverDomingo() throws FilmeSemEstoqueException, LocadoraException {
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		Filme filme = new Filme("Titanic", 2, 10.00);
		
		Usuario usuario = new Usuario("Douglas");
		
		Locacao resultado = locacaoService.alugarFilme(usuario, Arrays.asList(filme));
		
		//boolean isSegunda = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
		//assertTrue(isSegunda);
		
		//assertThat(resultado.getDataRetorno(), new DiaSemanaMatcher(Calendar.SUNDAY));
		assertThat(resultado.getDataRetorno(), MatchersProprios.paraDia(Calendar.MONDAY));
		assertThat(resultado.getDataRetorno(), MatchersProprios.paraSegunda());
	}
}
