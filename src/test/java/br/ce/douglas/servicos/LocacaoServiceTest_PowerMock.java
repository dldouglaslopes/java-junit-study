package br.ce.douglas.servicos;

import static org.junit.Assert.assertThat;

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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.ce.douglas.builders.FilmeBuilder;
import br.ce.douglas.builders.UsuarioBuilder;
import br.ce.douglas.daos.LocacaoDAO;
import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.matchers.MatchersProprios;
import br.ce.douglas.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocacaoService.class)
public class LocacaoServiceTest_PowerMock {
	
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
		locacaoService = PowerMockito.spy(locacaoService);
	}
	
	@Test
	public void alugarFilme () throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(6, 3, 2020));
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
		
		//error.checkThat(locacao.getValor(), CoreMatchers.is(CoreMatchers.equalTo(15.0)));
	    //error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), CoreMatchers.is(true));
	    //error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), CoreMatchers.is(true));
		
		//error.checkThat(locacao.getDataLocacao(), MatchersProprios.hoje());
		//error.checkThat(locacao.getDataRetorno(), MatchersProprios.hojeComDiferencaDias(1));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(6, 3, 2020)), CoreMatchers.is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(7, 3, 2020)), CoreMatchers.is(true));
	}
	
	@Test
	public void naoDevolverDomingo() throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		//PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(7, 3, 2020));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 7);
		calendar.set(Calendar.MONTH, Calendar.MARCH);
		calendar.set(Calendar.YEAR, 2020);
		PowerMockito.mockStatic(Calendar.class);
		PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
		
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);
		//boolean isSegunda = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
		//assertTrue(isSegunda);
		
		//assertThat(resultado.getDataRetorno(), new DiaSemanaMatcher(Calendar.SUNDAY));
		//assertThat(resultado.getDataRetorno(), MatchersProprios.paraDia(Calendar.MONDAY));
		assertThat(resultado.getDataRetorno(), MatchersProprios.paraSegunda());
		
		PowerMockito.verifyStatic(Mockito.times(2));
		Calendar.getInstance();
	}
	
	@Test
	public void alugarFilmeSemCalcularValor() throws Exception {
		Usuario usuario = UsuarioBuilder.umUsuario().agora();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		
		PowerMockito.doReturn(1.0).when(locacaoService, "calcularValorLocacao", filmes);
		
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
		
		Assert.assertThat(locacao.getValor(), CoreMatchers.is(1.0));
		PowerMockito.verifyPrivate(locacaoService).invoke("calcularValorLocacao", filmes);
	}
	
	@Test
	public void calcularValorLocacao() throws Exception {
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());
		Double valor = (Double) org.powermock.reflect.Whitebox.invokeMethod(locacaoService, "calcularValorLocacao", filmes);
		Assert.assertThat(valor, CoreMatchers.is(4.0));
	}
}
