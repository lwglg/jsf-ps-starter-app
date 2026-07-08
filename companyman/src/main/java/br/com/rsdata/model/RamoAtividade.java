package br.com.rsdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Ramo de atividade de uma empresa.
 * Um ramo de atividade pode ser associado a várias empresas (1:N).
 */
@Entity
@Table(name = "ramo_atividade", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ramo_atividade_descricao", columnNames = "descricao")
})
public class RamoAtividade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;


    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    public RamoAtividade() {
    }

    public RamoAtividade(String descricao) {
        this.descricao = descricao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Igualdade determinada pela descrição (chave de negócio), garantindo que
     * duas instâncias representando o mesmo ramo de atividade sejam consideradas iguais
     * mesmo antes da persistência (quando o id ainda não foi gerado).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RamoAtividade)) {
            return false;
        }
        RamoAtividade that = (RamoAtividade) o;
        return descricao != null
                ? descricao.equalsIgnoreCase(that.descricao)
                : that.descricao == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descricao != null ? descricao.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return "RamoAtividade{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}
