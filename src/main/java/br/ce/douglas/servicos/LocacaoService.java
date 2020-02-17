package br.ce.douglas.servicos;

import java.util.Date;

import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;
import br.ce.douglas.utils.DataUtils;

public class LocacaoService {
		
	public Locacao alugarFilme(Usuario usuario, Filme filme) throws FilmeSemEstoqueException, LocadoraException {
		
		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}
		
		if(filme == null) {
			throw new LocadoraException("Filme vazio");
		}
		
		if (filme.getEstoque() == 0) {
			throw new FilmeSemEstoqueException();
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilme(filme);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(filme.getPrecoLocacao());

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		//TODO adicionar método para salvar
		
		return locacao;
	}

	
}