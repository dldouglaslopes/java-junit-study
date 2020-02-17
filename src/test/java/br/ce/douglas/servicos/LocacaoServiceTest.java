package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;

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
		Filme filme = new Filme();
		filme.setEstoque(2);
		filme.setNome("Titanic");
		filme.setPrecoLocacao(10.00);
		Usuario usuario = new Usuario();
		usuario.setNome("Douglas");
		
		Locacao locacao = new LocacaoService().alugarFilme(usuario, filme);
		
		error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(10.0)));
	    error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), CoreMatchers.is(true));
	    error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), CoreMatchers.is(true));
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void filmeSemEstoque() throws Exception{
		Filme filme = new Filme();
		filme.setEstoque(0);
		filme.setNome("Titanic");
		filme.setPrecoLocacao(10.00);
		Usuario usuario = new Usuario();
		usuario.setNome("Douglas");
		
		locacaoService.alugarFilme(usuario, filme);
		
	}
	
	@Test
	public void usuarioVazio() throws FilmeSemEstoqueException{
		Filme filme = new Filme();
		filme.setEstoque(1);
		filme.setNome("Titanic");
		filme.setPrecoLocacao(10.00);
		
		try {
			locacaoService.alugarFilme(null, filme);
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), CoreMatchers.is("Usuario vazio"));
		}
		
	}
	
	@Test
	public void filmeVazio() throws FilmeSemEstoqueException, LocadoraException {
		Usuario usuario = new Usuario();
		usuario.setNome("Douglas");
		
		
		expectedException.expect(LocadoraException.class);
		expectedException.expectMessage("Filme vazio");
		
		locacaoService.alugarFilme(usuario, null);	
	}
	
}
