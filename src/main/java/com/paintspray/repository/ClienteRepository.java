package com.paintspray.repository;

import com.paintspray.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Camada de Acesso a Dados (DAO/Repository) para a entidade Cliente.
 * Agora com exclusão em cascata manual para remover também os veículos do cliente.
 */
public class ClienteRepository extends BaseRepository<Cliente> {

    /**
     * Salva um novo cliente
     */
    @Override
    public void save(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (id, nome, endereco, numero_telefone) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, cliente.getId());
            stmt.setString(2, cliente.getNome());
            stmt.setString(3, cliente.getEndereco());
            stmt.setString(4, cliente.getNumeroTelefone());

            stmt.executeUpdate();
        }
    }

    /**
     * Atualiza os dados de um cliente
     */
    @Override
    public void update(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET nome = ?, endereco = ?, numero_telefone = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getEndereco());
            stmt.setString(3, cliente.getNumeroTelefone());
            stmt.setString(4, cliente.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * EXCLUSÃO EM CASCATA:
     * Ao deletar um cliente → primeiro apaga todos os veículos
     */
    @Override
public void delete(String idCliente) throws SQLException {

    executeTransaction(connection -> {

        // 1. Apagar todos os serviços associados aos veículos deste cliente
        String deleteServicos = "DELETE FROM servicos WHERE id_veiculo IN (SELECT id FROM veiculos WHERE id_cliente = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(deleteServicos)) {
            stmt.setString(1, idCliente);
            stmt.executeUpdate();
        }

        // 2. Apagar todos os veículos associados ao cliente
        String deleteVeiculos = "DELETE FROM veiculos WHERE id_cliente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteVeiculos)) {
            stmt.setString(1, idCliente);
            stmt.executeUpdate();
        }

        // 3. Apagar o cliente
        String deleteCliente = "DELETE FROM clientes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteCliente)) {
            stmt.setString(1, idCliente);
            stmt.executeUpdate();
        }
    });
}

    /**
     * Busca um cliente pelo ID
     */
    @Override
    public Cliente findById(String id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        return findOne(sql, this::mapResultSetToCliente, id);
    }

    /**
     * Lista todos os clientes ordenados por nome
     */
    @Override
    public List<Cliente> findAll() throws SQLException {
        String sql = "SELECT * FROM clientes ORDER BY nome";
        return findMany(sql, this::mapResultSetToCliente);
    }

    /**
     * Converte uma linha do ResultSet em um objeto Cliente
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("endereco"),
                rs.getString("numero_telefone")
        );
    }
}
