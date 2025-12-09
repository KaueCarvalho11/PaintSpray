package com.paintspray.controller;

import com.paintspray.MainApplication;
import com.paintspray.model.Servico;
import com.paintspray.service.ServicoService;
import com.paintspray.service.ClienteService;
import com.paintspray.enums.StatusServico;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller da tela principal (Dashboard)
 */
public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox contentArea;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button clientesBtn;

    @FXML
    private Button servicosBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label welcomeLabel;

    private final ServicoService servicoService = new ServicoService();
    private final ClienteService clienteService = new ClienteService();

    @FXML
    private void initialize() {
        // Exibe nome do usuário logado
        if (SessionManager.isLogado()) {
            welcomeLabel.setText("Bem-vindo(a), " + SessionManager.getUsuarioLogado().getNome());
        }

        // Carrega Dashboard por padrão
        handleDashboard();
    }

    @FXML
    private void handleDashboard() {
        setActiveButton(dashboardBtn);
        loadDashboardView();
    }

    @FXML
    private void handleClientes() {
        setActiveButton(clientesBtn);
        loadClientesView();
    }

    @FXML
    private void handleServicos() {
        setActiveButton(servicosBtn);
        loadServicosView();
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try {
            MainApplication.loadLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNovaOrdem() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/paintspray/fxml/nova-ordem.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Nova Ordem de Serviço");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(mainBorderPane.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);

            dialog.showAndWait();

            // Recarrega dashboard após fechar
            if (dashboardBtn.getStyleClass().contains("active")) {
                loadDashboardView();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega a visualização do Dashboard (Kanban)
     */
    private void loadDashboardView() {
        contentArea.getChildren().clear();

        // KPIs no topo
        HBox kpiBox = createKPICards();
        contentArea.getChildren().add(kpiBox);

        // Kanban Board
        HBox kanbanBoard = createKanbanBoard();
        ScrollPane scrollPane = new ScrollPane(kanbanBoard);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * Cria os cards de KPI (indicadores)
     */
    private HBox createKPICards() {
        HBox kpiBox = new HBox(20);
        kpiBox.setAlignment(Pos.CENTER);
        kpiBox.setPadding(new Insets(20, 20, 10, 20));

        try {
            int total = servicoService.listarTodos().size();
            int pendentes = servicoService.listarServicosPendentes().size();
            int emAndamento = servicoService.listarServicosEmAndamento().size();
            int finalizados = servicoService.listarServicosFinalizados().size();

            kpiBox.getChildren().addAll(
                    createKPICard("Total de Serviços", String.valueOf(total), "#3498DB", "mdi2c-clipboard-text"),
                    createKPICard("Pendentes", String.valueOf(pendentes), "#F39C12", "mdi2c-clock-outline"),
                    createKPICard("Em Andamento", String.valueOf(emAndamento), "#2980B9", "mdi2p-progress-wrench"),
                    createKPICard("Finalizados", String.valueOf(finalizados), "#27AE60", "mdi2c-check-circle"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kpiBox;
    }

    /**
     * Cria um card de KPI individual
     */
    private VBox createKPICard(String label, String value, String color, String iconLiteral) {
        VBox card = new VBox(10);
        card.getStyleClass().add("kpi-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setPrefHeight(100);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(32);
        icon.setIconColor(javafx.scene.paint.Color.web(color));

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        card.getChildren().addAll(icon, valueLabel, titleLabel);
        return card;
    }

    /**
     * Cria o quadro Kanban com as colunas de status
     */
    private HBox createKanbanBoard() {
        HBox kanban = new HBox(15);
        kanban.setPadding(new Insets(20));
        kanban.setAlignment(Pos.TOP_LEFT);

        try {
            List<Servico> pendentes = servicoService.listarServicosPendentes();
            List<Servico> emAndamento = servicoService.listarServicosEmAndamento();
            List<Servico> aguardando = servicoService.listarServicosAguardandoPagamento();
            List<Servico> finalizados = servicoService.listarServicosFinalizados();

            kanban.getChildren().addAll(
                    createKanbanColumn("Pendente", pendentes, "pendente"),
                    createKanbanColumn("Em Andamento", emAndamento, "em-andamento"),
                    createKanbanColumn("Aguardando Pagamento", aguardando, "aguardando-pagamento"),
                    createKanbanColumn("Finalizado", finalizados, "finalizado"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kanban;
    }

    /**
     * Cria uma coluna do Kanban com suporte a drop
     */
    private VBox createKanbanColumn(String title, List<Servico> servicos, String statusClass) {
        VBox column = new VBox(10);
        column.getStyleClass().add("kanban-column");
        column.setPrefWidth(280);
        column.setMinHeight(400);
        column.setUserData(statusClass); // Armazena o status da coluna

        // Configurar drop target
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragEntered(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle("-fx-background-color: rgba(41, 128, 185, 0.1);");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle("");
            event.consume();
        });

        column.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int servicoId = Integer.parseInt(db.getString());
                StatusServico novoStatus = mapStatusClassToEnum(statusClass);

                try {
                    Servico servico = servicoService.exibirServico(servicoId);

                    // Validações de transição
                    if (!validarTransicaoStatus(servico.getStatus(), novoStatus)) {
                        servicoService.atualizarStatusServico(servicoId, novoStatus);
                        loadDashboardView(); // Recarrega o dashboard
                        success = true;
                    } else {
                        showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Transição Inválida",
                                "Não é possível mover este serviço para este status.");
                    }
                } catch (Exception e) {
                    showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Erro",
                            "Erro ao atualizar status: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Header da coluna
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kanban-column-header");
        Label countLabel = new Label("(" + servicos.size() + ")");
        countLabel.setStyle("-fx-text-fill: #7F8C8D;");
        header.getChildren().addAll(titleLabel, countLabel);

        column.getChildren().add(header);

        // Cards de serviço
        for (Servico servico : servicos) {
            column.getChildren().add(createServiceCard(servico, statusClass));
        }

        return column;
    }

    /**
     * Cria um card de serviço com drag-and-drop
     */
    private VBox createServiceCard(Servico servico, String statusClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("status-card", statusClass);
        card.setPadding(new Insets(12));
        card.setUserData(servico); // Armazena o serviço no card

        // Configurar drag source
        card.setOnDragDetected(event -> {
            javafx.scene.input.Dragboard db = card.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(String.valueOf(servico.getIdServico()));
            db.setContent(content);
            card.setOpacity(0.5);
            event.consume();
        });

        card.setOnDragDone(event -> {
            card.setOpacity(1.0);
            event.consume();
        });

        // Tipo de serviço (título)
        Label tipoLabel = new Label(servico.getTipo().getDescricao());
        tipoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Descrição
        Label descLabel = new Label(servico.getDescricao());
        descLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        // Preço
        Label precoLabel = new Label(String.format("R$ %.2f", servico.getPreco()));
        precoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");

        // Badge de status
        Label statusBadge = new Label(servico.getStatus().getDescricao());
        statusBadge.getStyleClass().addAll("badge", "badge-" + statusClass);

        card.getChildren().addAll(tipoLabel, descLabel, precoLabel, statusBadge);

        // Ação ao clicar
        card.setOnMouseClicked(event -> {
            mostrarDetalhesServico(servico);
        });

        return card;
    }

    /**
     * Placeholder para telas em desenvolvimento
     */
    private void showPlaceholder(String title, String iconLiteral) {
        contentArea.getChildren().clear();

        VBox placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(64);
        icon.setIconColor(javafx.scene.paint.Color.web("#95A5A6"));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #7F8C8D;");

        Label descLabel = new Label("Esta funcionalidade será implementada em breve.");
        descLabel.setStyle("-fx-text-fill: #95A5A6;");

        placeholder.getChildren().addAll(icon, titleLabel, descLabel);
        contentArea.getChildren().add(placeholder);
    }

    /**
     * Carrega a view de clientes
     */
    private void loadClientesView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/paintspray/fxml/clientes.fxml"));
            Parent clientesView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(clientesView);
            VBox.setVgrow(clientesView, Priority.ALWAYS);

        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder("Erro ao carregar Clientes", "mdi2a-alert-circle");
        }
    }

    /**
     * Carrega a view de serviços
     */
    private void loadServicosView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/paintspray/fxml/servicos.fxml"));
            Parent servicosView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(servicosView);
            VBox.setVgrow(servicosView, Priority.ALWAYS);

        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder("Erro ao carregar Serviços", "mdi2a-alert-circle");
        }
    }

    /**
     * Mostra dialog com detalhes do serviço
     */
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
        
        // Busca o nome do cliente pelo veículo
        String nomeCliente = "N/A";
        try {
            nomeCliente = clienteService.buscarClientePorId(servico.getVeiculo().getIdCliente()).getNome();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        info.getChildren().addAll(
                criarInfoRow("Tipo:", servico.getTipo().getDescricao()),
                criarInfoRow("Descrição:", servico.getDescricao()),
                criarInfoRow("Preço:", String.format("R$ %.2f", servico.getPreco())),
                criarInfoRow("Pagamento:", servico.getFormaPagamento().getDescricao()),
                new Separator(),
                criarInfoRow("Cliente:", nomeCliente),
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

    /**
     * Cria uma linha de informação para o dialog de detalhes
     */
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

    /**
     * Define o botão ativo no menu lateral
     */
    private void setActiveButton(Button activeBtn) {
        dashboardBtn.getStyleClass().remove("active");
        clientesBtn.getStyleClass().remove("active");
        servicosBtn.getStyleClass().remove("active");

        activeBtn.getStyleClass().add("active");
    }

    /**
     * Mapeia a classe CSS do status para o enum correspondente
     */
    private StatusServico mapStatusClassToEnum(String statusClass) {
        return switch (statusClass) {
            case "pendente" -> StatusServico.PENDENTE;
            case "em-andamento" -> StatusServico.EM_ANDAMENTO;
            case "aguardando-pagamento" -> StatusServico.AGUARDANDO_PAGAMENTO;
            case "finalizado" -> StatusServico.FINALIZADO;
            case "cancelado" -> StatusServico.CANCELADO;
            default -> null;
        };
    }

    /**
     * Valida se a transição entre status é inválida
     * Retorna true se a transição for INVÁLIDA, false se for VÁLIDA
     */
    private boolean validarTransicaoStatus(StatusServico atual, StatusServico novo) {
        // Se for para o mesmo status, é inválido
        if (atual == novo) {
            return true;
        }

        // Permite cancelar de qualquer status (exceto já cancelado)
        if (novo == StatusServico.CANCELADO && atual != StatusServico.CANCELADO) {
            return false;
        }

        // Validações de fluxo normal - retorna true se NÃO for permitido
        return switch (atual) {
            case PENDENTE -> novo != StatusServico.EM_ANDAMENTO && novo != StatusServico.CANCELADO;
            case EM_ANDAMENTO -> novo != StatusServico.AGUARDANDO_PAGAMENTO && novo != StatusServico.CANCELADO;
            case AGUARDANDO_PAGAMENTO -> novo != StatusServico.FINALIZADO && novo != StatusServico.CANCELADO;
            case FINALIZADO, CANCELADO -> true; // Não permite alterar serviços finalizados ou cancelados
        };
    }

    /**
     * Exibe um alerta para o usuário
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
