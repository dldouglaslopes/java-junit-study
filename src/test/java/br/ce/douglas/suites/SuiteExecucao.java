package br.ce.douglas.suites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.ce.douglas.servicos.CalculadoraTest;
import br.ce.douglas.servicos.CalculoValorLocacaoTest;
import br.ce.douglas.servicos.LocacaoServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
	CalculadoraTest.class,
	CalculoValorLocacaoTest.class,
	LocacaoServiceTest.class
})
public class SuiteExecucao {

	@BeforeClass
	public static void before() {
		System.out.println("before");
	}
	
	@AfterClass
	public static void after() {
		System.out.println("after");
	}
}
