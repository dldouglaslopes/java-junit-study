package br.ce.douglas.servicos;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;
import br.ce.douglas.utils.DataUtils;

public class LocacaoService {
		
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		
		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}
		
		if(filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Filme vazio");
		}
		
		for (Filme filme : filmes) {
			if (filme.getEstoque() == 0) {
				throw new FilmeSemEstoqueException();
			}
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		
		Double valorTotal = 0.0;
		
		for (int i = 0; i < filmes.size(); i++) {
			switch (i) {
			case 2:
				valorTotal += filmes.get(i).getPrecoLocacao()*0.75;
				break;
			case 3:
				valorTotal += filmes.get(i).getPrecoLocacao()*0.5;
				break;
			case 4:
				valorTotal += filmes.get(i).getPrecoLocacao()*0.25;
				break;
			case 5:
				valorTotal += filmes.get(i).getPrecoLocacao()*0;
				break;
			default:
				valorTotal += filmes.get(i).getPrecoLocacao();
				break;
			}
		}

		locacao.setValor(valorTotal);
		
		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		//TODO adicionar método para salvar
		
		return locacao;
	}

	
}