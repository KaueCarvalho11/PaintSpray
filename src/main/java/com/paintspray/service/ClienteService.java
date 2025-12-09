package com.paintspray.service;

import com.paintspray.model.Cliente;
import com.paintspray.model.Servico;
import com.paintspray.repository.ClienteRepository;
import com.paintspray.repository.ServicoRepository;
import com.paintspray.repository.VeiculoRepository;
import java.sql.SQLException;
import java.util.List;

/**
 * Camada de Serviço para a entidade Cliente.
 * Responsável por conter as regras de negócio, validações de dados
 * e orquestrar as operações, delegando a persistência para a camada de
 * repositório.
 */
public class ClienteService {

    // Dependências necessárias para as operações de cliente.
    private final ClienteRepository repository;
    private final ServicoRepository servicoRepository;
    private final VeiculoRepository veiculoRepository;

    public ClienteService() {
        this.repository = new ClienteRepository();
        this.servicoRepository = new ServicoRepository();
        this.veiculoRepository = new VeiculoRepository();
    }

    /**
     * Valida os dados e cadastra um novo cliente.
     */
    public void cadastrarCliente(String id, String nome, String email, String senha, String endereco, String telefone)
            throws SQLException, IllegalArgumentException {
        // Bloco de validações dos dados de entrada
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("O ID do cliente não pode ser vazio.");

        // só dígitos e não pode começar com zero
        if (!id.matches("^[1-9]\\d{0,9}$"))
            throw new IllegalArgumentException("ID deve ser número inteiro positivo sem zeros à esquerda.");

        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("O nome não pode ser vazio.");

        if (!nome.matches("^[A-Za-zÀ-ÖØ-öø-ÿ ]+$"))
            throw new IllegalArgumentException("O nome só pode conter letras e espaços.");

        if (nome.contains("  "))
            throw new IllegalArgumentException("O nome não pode conter espaços duplos.");

        if (endereco == null || endereco.trim().isEmpty())
            throw new IllegalArgumentException("O endereço não pode ser vazio.");

        if (!endereco.matches("^[A-Za-zÀ-ÖØ-öø-ÿ0-9 ,.-]+$"))
            throw new IllegalArgumentException(
                    "O endereço só pode conter letras, números, espaços, vírgulas, pontos e hífens.");

        if (endereco.contains("  "))
            throw new IllegalArgumentException("O endereço não pode conter espaços duplos.");

        if (telefone == null || telefone.trim().isEmpty())
            throw new IllegalArgumentException("O telefone não pode ser vazio.");

        if (!telefone.matches("^\\+?\\d{8,15}$"))
            throw new IllegalArgumentException("Telefone inválido. Deve conter apenas dígitos (8–15 caracteres), "
                    + "podendo começar com '+' para código de país.");

        // Regra de negócio: não permitir cadastro com ID ou email duplicado
        if (repository.findById(id) != null)
            throw new IllegalArgumentException("Já existe um cliente com este ID.");

        // Se tudo estiver válido, cria o objeto e delega a persistência ao repositório
        Cliente novoCliente = new Cliente(id, nome, endereco, telefone);
        repository.save(novoCliente);
    }

    /**
     * Cadastra um novo cliente (versão sobrecarregada que aceita objeto Cliente).
     */
    public void cadastrarCliente(Cliente cliente) throws SQLException {
        // Validações simplificadas para cadastro via interface
        if (cliente.getId() == null || cliente.getId().trim().isEmpty())
            throw new IllegalArgumentException("O ID do cliente não pode ser vazio.");

        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty())
            throw new IllegalArgumentException("O nome não pode ser vazio.");

        if (cliente.getEndereco() == null || cliente.getEndereco().trim().isEmpty())
            throw new IllegalArgumentException("O endereço não pode ser vazio.");

        if (cliente.getNumeroTelefone() == null || cliente.getNumeroTelefone().trim().isEmpty())
            throw new IllegalArgumentException("O telefone não pode ser vazio.");

        // Regra de negócio: não permitir cadastro com ID duplicado
        if (repository.findById(cliente.getId()) != null)
            throw new IllegalArgumentException("Já existe um cliente com este ID.");

        // Salva diretamente o cliente
        repository.save(cliente);
    }

    /**
     * Retorna uma lista com todos os clientes.
     */
    public List<Cliente> listarTodosClientes() throws SQLException {
        return repository.findAll();
    }

    /**
     * Retorna uma lista com todos os clientes (alias).
     */
    public List<Cliente> listarTodos() throws SQLException {
        return repository.findAll();
    }

    /**
     * Busca um cliente pelo ID. Lança uma exceção se não for encontrado.
     */
    public Cliente buscarClientePorId(String id) throws SQLException, IllegalArgumentException {
        Cliente cliente = repository.findById(id);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente com o ID " + id + " não encontrado.");
        }
        return cliente;
    }

    /**
     * Valida e atualiza os dados de um cliente existente.
     */
    public void atualizarCliente(String id, String novoNome, String novoEmail, String novaSenha, String novoEndereco,
            String novoTelefone) throws SQLException, IllegalArgumentException {
        // Primeiro, busca o cliente para garantir que ele existe
        Cliente cliente = buscarClientePorId(id);

        if (novoNome == null || novoNome.trim().isEmpty())
            throw new IllegalArgumentException("Nome inválido");
        if (novoEndereco == null || novoEndereco.trim().isEmpty())
            throw new IllegalArgumentException("Endereço inválido");
        if (novoTelefone == null || novoTelefone.trim().isEmpty())
            throw new IllegalArgumentException("Telefone inválido");

        // Atualiza o objeto com os novos dados
        cliente.setNome(novoNome);
        cliente.setEndereco(novoEndereco);
        cliente.setNumeroTelefone(novoTelefone);

        repository.update(cliente);
    }

    /**
     * Atualiza um cliente (versão sobrecarregada que aceita objeto Cliente).
     */
    public void atualizarCliente(Cliente cliente) throws SQLException {
        atualizarCliente(cliente.getId(), cliente.getNome(), "", "",
                cliente.getEndereco(), cliente.getNumeroTelefone());
    }


   /**
 * Remove um cliente do sistema (exclusão em cascata feita no repositório).
 */
public void removerCliente(String id) throws SQLException, IllegalArgumentException {
    // Garante que o cliente existe
    buscarClientePorId(id);

    // O repositório faz a exclusão em cascata (serviços -> veículos -> cliente)
    repository.delete(id);
}

    /**
     * Retorna uma lista de todos os serviços solicitados por um cliente específico.
     */
    public List<Servico> verServicosSolicitados(String clienteId) throws SQLException {

        buscarClientePorId(clienteId);

        return servicoRepository.findByClienteId(clienteId);
    }
}