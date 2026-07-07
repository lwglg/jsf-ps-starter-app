package br.com.rsdata.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("RamoAtividade - regras de domínio")
class RamoAtividadeTest {

    @Test
    @DisplayName("Duas instâncias com a mesma descrição (case-insensitive) devem ser iguais")
    void deveSeremIguaisPelaDescricao() {
        RamoAtividade r1 = new RamoAtividade("Tecnologia da Informação");
        RamoAtividade r2 = new RamoAtividade("tecnologia da informação");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("Instâncias com descrições diferentes não devem ser iguais")
    void naoDeveSeremIguaisComDescricoesDiferentes() {
        RamoAtividade r1 = new RamoAtividade("Tecnologia da Informação");
        RamoAtividade r2 = new RamoAtividade("Comércio Varejista");

        assertNotEquals(r1, r2);
    }

    @Test
    @DisplayName("Igualdade não deve depender do id gerado")
    void igualdadeIndependeDoId() {
        RamoAtividade r1 = new RamoAtividade("Construção Civil");
        r1.setId(UUID.randomUUID());

        RamoAtividade r2 = new RamoAtividade("Construção Civil");
        r2.setId(UUID.randomUUID());

        assertEquals(r1, r2);
    }

    @Test
    @DisplayName("Getters e setters devem funcionar corretamente")
    void gettersSettersFuncionaisCorretamente() {
        RamoAtividade ramo = new RamoAtividade();
        UUID id = UUID.randomUUID();
        ramo.setId(id);
        ramo.setDescricao("Serviços de Consultoria");

        assertEquals(id, ramo.getId());
        assertEquals("Serviços de Consultoria", ramo.getDescricao());
    }
}
