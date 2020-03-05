package br.ce.douglas.servicos;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.douglas.daos.LocacaoDAO;
import br.ce.douglas.entidades.Filme;
import br.ce.douglas.entidades.Locacao;
import br.ce.douglas.entidades.Usuario;
import br.ce.douglas.exceptions.FilmeSemEstoqueException;
import br.ce.douglas.exceptions.LocadoraException;
import br.ce.douglas.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDAO dao;
	private SPCService spcService;
	private EmailService emailService;
	
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
		
		boolean negativado;
		
		try {
			negativado = spcService.possuiNegativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas com SPC, tente novamente");
		}
		
		if (negativado) {
			throw new LocadoraException("Usuario Negativado");
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(Calendar.getInstance().getTime());
		
		Double valorTotal = calcularValorLocacao(filmes);

		locacao.setValor(valorTotal);
		
		//Entrega no dia seguinte
		Date dataEntrega = Calendar.getInstance().getTime();
		dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		
		return locacao;
	}

	private Double calcularValorLocacao(List<Filme> filmes) {
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
		return valorTotal;
	}
	
	public void notificarAtrasos() {
		List<Locacao> locacoes = dao.obterLocacoesPendentes();
		
		for (Locacao locacao : locacoes) {
			if (locacao.getDataRetorno().before(new Date())) {
				emailService.notificaAtraso(locacao.getUsuario());
			}
		}
	}

	public void prorrogarLocacao(Locacao locacao, int dias) {
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(new Date());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor()*dias);
		dao.salvar(novaLocacao);
	}
}