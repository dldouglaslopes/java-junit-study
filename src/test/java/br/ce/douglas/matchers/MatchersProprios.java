package br.ce.douglas.matchers;

import java.util.Calendar;

import org.hamcrest.Matcher;

public class MatchersProprios {
	public static DiaSemanaMatcher paraDia(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}
	
	public static DiaSemanaMatcher paraSegunda() {
		return new DiaSemanaMatcher(Calendar.MONDAY);
	}

	public static Matcher hojeComDiferencaDias(int qtdDias) {
		return new DataDiferencaDiasMatcher(qtdDias);
	}
	
	public static Matcher hoje() {
		return new DataDiferencaDiasMatcher(0);
	}
}
