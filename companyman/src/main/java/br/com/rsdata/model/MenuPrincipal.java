package br.com.rsdata.model;

import java.util.List;
import java.util.Optional;

public class MenuPrincipal {
    public static record SubMenuItemRecord(
        String titulo,
        String icone,
        Optional<String> outcome,
        Optional<String> onClickHandler
    ) {
        public SubMenuItemRecord {
            if (titulo == null) throw new NullPointerException("O títuto do item de submenu deve ser informado");
            if (icone == null) throw new NullPointerException("O ícone do item de submenu deve ser informado");
        }    
    }

    public static record SubMenuRecord(
        String titulo,
        String icone,
        List<SubMenuItemRecord> items
    ) {}
}
