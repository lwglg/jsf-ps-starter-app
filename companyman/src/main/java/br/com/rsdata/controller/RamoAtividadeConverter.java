package br.com.rsdata.controller;

import br.com.rsdata.model.RamoAtividade;
import br.com.rsdata.service.RamoAtividadeService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.util.UUID;

/**
 * Converte o UUID (String) selecionado no componente p:selectOneMenu de volta
 * para uma instância de RamoAtividade gerenciada pela persistência.
 */
@FacesConverter(value = "ramoAtividadeConverter", managed = true)
public class RamoAtividadeConverter implements Converter<RamoAtividade> {

    private final RamoAtividadeService service = new RamoAtividadeService();

    @Override
    public RamoAtividade getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return service.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, RamoAtividade value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}
