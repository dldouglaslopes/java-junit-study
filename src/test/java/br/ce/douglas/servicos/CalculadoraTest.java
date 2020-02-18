package br.ce.douglas.servicos;

import org.junit.Before;
import org.junit.Test;

import br.ce.douglas.exceptions.NaoPodeDividirPorZeroException;
import junit.framework.Assert;

public class CalculadoraTest {
	
	private Calculadora calc;
	
	@Before
	public void setup() {
		calc = new Calculadora();
	}

	@Test
	public void somaDoisValores() {
		int a = 5;
		int b = 3;
		
		Calculadora calc = new Calculadora();
		
		int resultado = calc.somar(a, b);
		Assert.assertEquals(8, resultado);
	}
	
	@Test
	public void subtraiDoisValores() {
		int a = 5;
		int b = 3;
		
		Calculadora calc = new Calculadora();
		
		int resultado = calc.subtrair(a, b);
		Assert.assertEquals(2, resultado);
	}
	
	@Test
	public void divideDoisValores() throws NaoPodeDividirPorZeroException {
		int a = 5;
		int b = 3;
		
		Calculadora calc = new Calculadora();
		
		int resultado = calc.dividir(a, b);
		Assert.assertEquals(1, resultado);
	}
	
	@Test(expected = NaoPodeDividirPorZeroException.class)
	public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException {
		int a = 5;
		int b = 0;
		
		Calculadora calc = new Calculadora();
		
		int resultado = calc.dividir(a, b);
	}
}
