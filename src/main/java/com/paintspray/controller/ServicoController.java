package com.paintspray.controller;

import com.paintspray.enums.StatusServico;
import com.paintspray.model.Servico;
import com.paintspray.service.ServicoService;
import com.paintspray.service.ClienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller da tela de Ordens de Serviço (Lista completa)
 */
public class ServicoController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private TableView<Servico> servicoTable;

    @FXML
    private TableColumn<Servico, Integer> idColumn;

    @FXML
    private TableColumn<Servico, String> tipoColumn;

    @FXML
    private TableColumn<Servico, String> descricaoColumn;

    @FXML
    private TableColumn<Servico, Double> precoColumn;

    @FXML
    private TableColumn<Servico, String> statusColumn;

    @FXML
    private TableColumn<Servico, String> veiculoColumn;

    @FXML
    private TableColumn<Servico, String> clienteColumn;

    private final ServicoService servicoService = new ServicoService();
    private final ClienteService clienteService = new ClienteService();
    private ObservableList<Servico> servicos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configura colunas da tabela
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idServico"));
        tipoColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTipo().getDescricao()));
        descricaoColumn.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        precoColumn.setCellValueFactory(new PropertyValueFactory<>("preco"));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDescricao()));
        veiculoColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getVeiculo().getModelo() + " - " + cellData.getValue().getVeiculo().getCor()));
        clienteColumn.setCellValueFactory(cellData -> {
            try {
                String idCliente = cellData.getValue().getVeiculo().getIdCliente();
                String nomeCliente = clienteService.buscarClientePorId(idCliente).getNome();
                return new javafx.beans.property.SimpleStringProperty(nomeCliente);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        // Formata coluna de preço
        precoColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double preco, boolean empty) {
                super.updateItem(preco, empty);
                if (empty || preco == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", preco));
                }
            }
        });

        // Estiliza coluna de status
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    // Aplica cores baseado no status
                    if (status.contains("Pendente")) {
                        setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                    } else if (status.contains("Andamento")) {
                        setStyle("-fx-text-fill: #2980B9; -fx-font-weight: bold;");
                    } else if (status.contains("Aguardando")) {
                        setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");
                    } else if (status.contains("Finalizado")) {
                        setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                    } else if (status.contains("Cancelado")) {
                        setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Popula filtro de status
        statusFilterCombo.getItems().addAll("Todos", "Pendente", "Em Andamento",
                "Aguardando Pagamento", "Finalizado", "Cancelado");
        statusFilterCombo.setValue("Todos");

        // Listener para busca
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filtrarServicos());
        statusFilterCombo.setOnAction(e -> filtrarServicos());

        // Double-click para abrir detalhes
        servicoTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && servicoTable.getSelectionModel().getSelectedItem() != null) {
                handleDetalhes();
            }
        });

        // Carrega serviços
        carregarServicos();
    }

    private void carregarServicos() {
        try {
            List<Servico> lista = servicoService.listarTodos();
            servicos.clear();
            servicos.addAll(lista);
            servicoTable.setItems(servicos);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao carregar serviços: " + e.getMessage());
        }
    }

    private void filtrarServicos() {
        String busca = searchField.getText().toLowerCase();
        String statusFiltro = statusFilterCombo.getValue();

        try {
            List<Servico> todos = servicoService.listarTodos();
            List<Servico> filtrados = todos.stream()
                    .filter(s -> {
                        boolean matchBusca = busca.isEmpty() ||
                                s.getDescricao().toLowerCase().contains(busca) ||
                                s.getTipo().getDescricao().toLowerCase().contains(busca) ||
                                s.getVeiculo().getModelo().toLowerCase().contains(busca);

                        boolean matchStatus = statusFiltro.equals("Todos") ||
                                s.getStatus().getDescricao().equals(statusFiltro);

                        return matchBusca && matchStatus;
                    })
                    .toList();

            servicos.clear();
            servicos.addAll(filtrados);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao filtrar serviços: " + e.getMessage());
        }
    }

    @FXML
    private void handleDetalhes() {
        Servico selecionado = servicoTable.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um serviço.");
            return;
        }

        mostrarDetalhesServico(selecionado);
    }

    @FXML
    private void handleIniciar() {
        Servico selecionado = servicoTable.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um serviço.");
            return;
        }

        if (selecionado.getStatus() != StatusServico.PENDENTE) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Apenas serviços pendentes podem ser iniciados.");
            return;
        }

        try {
            servicoService.iniciarServico(selecionado.getIdServico());
            carregarServicos();
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Serviço iniciado!");
        } catch (SQLException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    private void handleAguardarPagamento() {
        Servico selecionado = servicoTable.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um serviço.");
            return;
        }

        if (selecionado.getStatus() != StatusServico.EM_ANDAMENTO) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Apenas serviços em andamento podem aguardar pagamento.");
            return;
        }

        try {
            servicoService.atualizarStatusServico(selecionado.getIdServico(), StatusServico.AGUARDANDO_PAGAMENTO);
            carregarServicos();
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Serviço aguardando pagamento!");
        } catch (SQLException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    private void handleConcluir() {
        Servico selecionado = servicoTable.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um serviço.");
            return;
        }

        if (selecionado.getStatus() == StatusServico.FINALIZADO ||
                selecionado.getStatus() == StatusServico.CANCELADO) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Este serviço já foi finalizado ou cancelado.");
            return;
        }

        try {
            servicoService.concluirServico(selecionado.getIdServico());
            carregarServicos();
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Serviço concluído!");
        } catch (SQLException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        Servico selecionado = servicoTable.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um serviço.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Cancelamento");
        confirmacao.setHeaderText("Deseja realmente cancelar este serviço?");
        confirmacao.setContentText("Esta ação não pode ser desfeita.");

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                servicoService.cancelarServico(selecionado.getIdServico());
                carregarServicos();
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Serviço cancelado!");
            } catch (SQLException | IllegalStateException e) {
                showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
            }
        }
    }

    private void mostrarDetalhesServico(Servico servico) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalhes do Serviço");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // Header com ID e Status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("OS #" + servico.getIdServico());
        idLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label statusBadge = new Label(servico.getStatus().getDescricao());
        String badgeStyle = switch (servico.getStatus()) {
            case PENDENTE -> "-fx-background-color: #F39C12;";
            case EM_ANDAMENTO -> "-fx-background-color: #2980B9;";
            case AGUARDANDO_PAGAMENTO -> "-fx-background-color: #E67E22;";
            case FINALIZADO -> "-fx-background-color: #27AE60;";
            case CANCELADO -> "-fx-background-color: #E74C3C;";
        };
        statusBadge.setStyle(badgeStyle + " -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15;");

        header.getChildren().addAll(idLabel, statusBadge);

        // Informações
        VBox info = new VBox(10);
        info.getChildren().addAll(
                criarInfoRow("Tipo:", servico.getTipo().getDescricao()),
                criarInfoRow("Descrição:", servico.getDescricao()),
                criarInfoRow("Preço:", String.format("R$ %.2f", servico.getPreco())),
                criarInfoRow("Pagamento:", servico.getFormaPagamento().getDescricao()),
                new Separator(),
                criarInfoRow("Veículo:", String.format("%s - %s (%d)",
                        servico.getVeiculo().getModelo(),
                        servico.getVeiculo().getCor(),
                        servico.getVeiculo().getAnoFabricacao())),
                criarInfoRow("Responsável:", servico.getUsuario().getNome()));

        content.getChildren().addAll(header, info);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private HBox criarInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 100px;");

        Label valueNode = new Label(value);
        valueNode.setWrapText(true);

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
