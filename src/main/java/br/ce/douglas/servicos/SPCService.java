package br.ce.douglas.servicos;

import br.ce.douglas.entidades.Usuario;

public interface SPCService {
	public boolean possuiNegativacao(Usuario usuario) throws Exception;
}
