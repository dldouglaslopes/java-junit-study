package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {
	private LocacaoService locacaoService;
	
	@Parameter
	public List<Filme> filmes;
	
	@Parameter(value=1)
	public Double valorLocacao;
	
	@Parameter(value=2)
	public String cenario;
	
	@Before
	public void setup() {
		locacaoService = new LocacaoService();
	}
	
	private static Filme filme1 = new Filme("Filme 1", 2, 4.0);
	private static Filme filme2 = new Filme("Filme 2", 2, 4.0);
	private static Filme filme3 = new Filme("Filme 3", 2, 4.0);
	private static Filme filme4 = new Filme("Filme 4", 2, 4.0);
	private static Filme filme5 = new Filme("Filme 5", 2, 4.0);
	private static Filme filme6 = new Filme("Filme 6", 2, 4.0);
	private static Filme filme7 = new Filme("Filme 7", 2, 4.0);
	
	@Parameters(name="{2}")
	public static Collection<Object[]> getParametros(){
		return Arrays.asList(new Object[][] {
			{Arrays.asList(filme1,filme2,filme3), 11.0, "3 filmes: 25%"},
			{Arrays.asList(filme1,filme2,filme3,filme4), 13.0, "4 filmes: 50%"},
			{Arrays.asList(filme1,filme2,filme3,filme4,filme5), 14.0, "5 filmes: 75%"},
			{Arrays.asList(filme1,filme2,filme3,filme4,filme5,filme6), 14.0, "6 filmes: 100%"},
			{Arrays.asList(filme1,filme2,filme3,filme4,filme5,filme6,filme7), 18.0, "7 filmes: 100%"}
		});
	}
	
	@Test
	public void calcularValorLocacaoComDescontos() throws FilmeSemEstoqueException, LocadoraException{
		Usuario usuario = new Usuario("Douglas");
		
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);
		
		assertThat(resultado.getValor(), CoreMatchers.is(valorLocacao));
		
		System.out.println("!");
	}
	
	@Test
	public void exibirValorALocacao() {
		System.out.println(valorLocacao);
	}
}
