package br.ce.douglas.matchers;

import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.douglas.utils.DataUtils;

public class DataDiferencaDiasMatcher extends TypeSafeMatcher<Date>{

	private Integer qtdDias;
	
	public DataDiferencaDiasMatcher(Integer qtdDias) {
		this.qtdDias = qtdDias;
	}
	
	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean matchesSafely(Date data) {
		return DataUtils.isMesmaData(data, DataUtils.obterDataComDiferencaDias(qtdDias));
	}

}
