package br.ce.douglas.servicos;

import br.ce.douglas.entidades.Usuario;

public interface EmailService {
	public void notificaAtraso(Usuario usuario);
}
