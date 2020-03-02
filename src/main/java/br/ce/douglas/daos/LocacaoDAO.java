package br.ce.douglas.daos;

import java.util.List;

import br.ce.douglas.entidades.Locacao;

public interface LocacaoDAO {

	public void salvar(Locacao locacao);

	public List<Locacao> obterLocacoesPendentes();
}
