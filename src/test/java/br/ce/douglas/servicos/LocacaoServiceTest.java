package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;
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
	}
	
	@Test
	public void teste () throws Exception {
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 3, 5.00);
		Usuario usuario = new Usuario("Douglas");
		
		Locacao locacao = new LocacaoService().alugarFilme(usuario, Arrays.asList(filme, filme2));
		
		error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(15.0)));
	    error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), CoreMatchers.is(true));
	    error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), CoreMatchers.is(true));
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
	public void pagar75PctFilme3() throws FilmeSemEstoqueException, LocadoraException{
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 2, 5.00);
		Filme filme3 = new Filme("Peixe", 2, 12.00);
		Usuario usuario = new Usuario("Douglas");
		
		Locacao resultado = locacaoService.alugarFilme(usuario, Arrays.asList(filme, filme2, filme3));
		
		assertThat(resultado.getValor(), CoreMatchers.is(24.00));
	}
	
	@Test
	public void pagar50PctFilme4() throws FilmeSemEstoqueException, LocadoraException{
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 2, 5.00);
		Filme filme3 = new Filme("Peixe", 2, 12.00);
		Filme filme4 = new Filme("Peixe", 2, 10.00);
		Usuario usuario = new Usuario("Douglas");
		
		Locacao resultado = locacaoService.alugarFilme(usuario, Arrays.asList(filme, filme2, filme3, filme4));
		
		assertThat(resultado.getValor(), CoreMatchers.is(29.00));
	}
	
	@Test
	public void pagar25PctFilme5() throws FilmeSemEstoqueException, LocadoraException{
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 2, 5.00);
		Filme filme3 = new Filme("Peixe", 2, 12.00);
		Filme filme4 = new Filme("Peixe", 2, 10.00);
		Filme filme5 = new Filme("Peixe", 2, 12.00);
		
		Usuario usuario = new Usuario("Douglas");
		
		Locacao resultado = locacaoService.alugarFilme(usuario, Arrays.asList(filme, filme2, filme3, filme4, filme5));
		
		assertThat(resultado.getValor(), CoreMatchers.is(32.00));
	}
	
	@Test
	public void pagar0PctFilme6() throws FilmeSemEstoqueException, LocadoraException{
		Filme filme = new Filme("Titanic", 2, 10.00);
		Filme filme2 = new Filme("Carros", 2, 5.00);
		Filme filme3 = new Filme("Peixe", 2, 12.00);
		Filme filme4 = new Filme("Peixe", 2, 10.00);
		Filme filme5 = new Filme("Peixe", 2, 12.00);
		Filme filme6 = new Filme("Peixe", 2, 12.00);
		
		Usuario usuario = new Usuario("Douglas");
		
		Locacao resultado = locacaoService.alugarFilme(usuario, Arrays.asList(filme, filme2, filme3, filme4, filme5, filme6));
		
		assertThat(resultado.getValor(), CoreMatchers.is(32.00));
	}
}
