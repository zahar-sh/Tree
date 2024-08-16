package com.example.tree.controller;

import com.example.tree.core.AvlTree;
import com.example.tree.core.Tree;
import com.example.tree.core.Trees;
import com.example.tree.exception.LoadingException;
import com.example.tree.exception.ValidationException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.synedra.validatorfx.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainController implements Controller {

    private static final String FXML_PATH = "main-view.fxml";

    private static final String ADD_KEY = "add";
    private static final String CONTAINS_KEY = "contains";
    private static final String REMOVE_KEY = "remove";
    private static final String NODE_WIDTH_KEY = "node-width";
    private static final String NODE_HEIGHT_KEY = "node-height";

    private static final Pattern OPTIONAL_NUMBER = Pattern.compile("[-+]?\\d*");

    public static MainController load(ControllerLoader loader) throws LoadingException {
        return loader.load(FXML_PATH);
    }

    @FXML
    private Pane root;

    @FXML
    private Canvas canvas;

    @FXML
    private TextField addTextField;

    @FXML
    private TextField containsTextField;

    @FXML
    private TextField removeTextField;

    @FXML
    private Button formatButton;

    @FXML
    private Button clearButton;

    @FXML
    private TextField nodeWidthTextField;

    @FXML
    private TextField nodeHeightTextField;

    @FXML
    private ColorPicker nodeColorColorPicker;

    @FXML
    private ColorPicker selectedColorPicker;

    @FXML
    private ColorPicker linksColorPicker;

    private Tree<Integer> tree;

    private Map<Integer, Point> pointMap;

    private Integer selectedNodeValue;

    private Size nodeSize;

    private Font font;

    private double lineWidth;

    //  difference between selected node pos and mouse pressed
    private Point delta;

    private Point selectedNodePos;

    @FXML
    private void initialize() {
        tree = new AvlTree<>(Integer::compareTo);
        pointMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            tree.add(i);
        }

        nodeSize = new Size(50, 50);

        font = Font.font("Consolas", FontWeight.BOLD, 14);
        lineWidth = 1.5;

        delta = new Point();

        nodeWidthTextField.setText(Integer.toString(nodeSize.getWidth()));
        nodeHeightTextField.setText(Integer.toString(nodeSize.getHeight()));
        nodeColorColorPicker.setValue(Color.CHOCOLATE);
        selectedColorPicker.setValue(Color.YELLOW);
        linksColorPicker.setValue(Color.ORANGE);

        Validator validator = new Validator();
        validateDigits(validator, ADD_KEY, addTextField);
        validateDigits(validator, CONTAINS_KEY, containsTextField);
        validateDigits(validator, REMOVE_KEY, removeTextField);
        validateDigits(validator, NODE_WIDTH_KEY, nodeWidthTextField);
        validateDigits(validator, NODE_HEIGHT_KEY, nodeHeightTextField);

        addTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::add))));
        containsTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::contains))));
        removeTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::remove))));
        nodeWidthTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::setWidth))));
        nodeHeightTextField.setOnKeyPressed(onEnterPressed(runAsync(suppressValidationExceptions(this::setHeight))));
        formatButton.setOnAction(onAction(runAsync(() -> {
            format();
            repaint();
        })));
        clearButton.setOnAction(onAction(runAsync(this::clear)));
        nodeColorColorPicker.setOnAction(onAction(runAsync(this::repaint)));
        selectedColorPicker.setOnAction(onAction(runAsync(this::repaint)));
        linksColorPicker.setOnAction(onAction(runAsync(this::repaint)));

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);

        format();
        repaint();
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    private void add() throws ValidationException {
        Integer value = getInt(addTextField);
        if (tree.add(value)) {
            pointMap.put(value, centerPoint());
            selectedNodeValue = value;
            format();
            repaint();
        }
    }

    private void contains() throws ValidationException {
        selectedNodeValue = null;
        int value;
        try {
            value = getInt(containsTextField);
        } catch (ValidationException e) {
            repaint();
            throw e;
        }
        if (tree.contains(value)) {
            selectedNodeValue = value;
        }
        repaint();
    }

    private void remove() throws ValidationException {
        Integer value = getInt(removeTextField);
        if (tree.remove(value)) {
            pointMap.remove(value);
            format();
            repaint();
        }
    }

    private void setWidth() throws ValidationException {
        int width = getInt(nodeWidthTextField);
        nodeSize.setWidth(width);
        repaint();
    }

    private void setHeight() throws ValidationException {
        int value = getInt(nodeHeightTextField);
        nodeSize.setHeight(value);
        repaint();
    }

    private void clear() {
        tree.clear();
        pointMap.clear();
        selectedNodeValue = null;
        repaint();
    }

    private void format() {
        int spaceY = (int) (nodeSize.getHeight() * 1.5);
        int screenWidth = (int) (canvas.getWidth() - nodeSize.getWidth());
        List<List<Tree.Node<Integer>>> levels = Trees.levels(tree.root());
        for (int level = 0; level < levels.size(); level++) {
            List<Tree.Node<Integer>> nodes = levels.get(level);
            int posY = level * spaceY;
            double posXFactor = 1 + Math.pow(2, level);
            for (int index = 0, s = nodes.size(); index < s; index++) {
                Tree.Node<Integer> node = nodes.get(index);
                Point pos = pointMap.computeIfAbsent(node.getValue(), v -> new Point());
                int posX = (int) ((index + 1) * (screenWidth / posXFactor));
                pos.setX(posX);
                pos.setY(posY);
                clampBounds(pos);
            }
        }
        levels.clear();
    }

    private int getInt(TextField textField) throws ValidationException {
        int value;
        try {
            value = Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            throw new ValidationException(e);
        }
        return value;
    }

    private void validateDigits(Validator validator, String key, TextField removeTextField) {
        validator.createCheck()
                .dependsOn(key, removeTextField.textProperty())
                .withMethod(context -> {
                    String value = context.get(key);
                    if (!OPTIONAL_NUMBER.matcher(value).matches()) {
                        context.error("Should be a number");
                    }
                })
                .decorates(removeTextField)
                .immediate();
    }

    private EventHandler<ActionEvent> onAction(Runnable action) {
        return keyEvent -> {
            action.run();
        };
    }

    private EventHandler<KeyEvent> onEnterPressed(Runnable action) {
        return keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                action.run();
            }
        };
    }

    private Runnable suppressValidationExceptions(ValidationRunnable action) {
        return () -> {
            try {
                action.run();
            } catch (ValidationException ignored) {
            }
        };
    }

    private Runnable runAsync(Runnable action) {
        return () -> {
            Platform.runLater(action);
        };
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        Integer oldSelectedNodeValue = selectedNodeValue;
        if (mouseEvent.isSecondaryButtonDown()) {
            selectedNodeValue = null;
        }
        int nodeWidth = nodeSize.getWidth();
        int nodeHeight = nodeSize.getHeight();
        int mouseX = (int) mouseEvent.getX();
        int mouseY = (int) mouseEvent.getY();
        for (Map.Entry<Integer, Point> entry : pointMap.entrySet()) {
            Point pos = entry.getValue();
            int posX = pos.getX();
            int posY = pos.getY();
            if (Geometry.containsPoint(posX, posY, posX + nodeWidth, posY + nodeHeight, mouseX, mouseY)) {
                selectedNodePos = pos;
                delta.setX(posX - mouseX);
                delta.setY(posY - mouseY);
                if (mouseEvent.isSecondaryButtonDown()) {
                    Integer value = entry.getKey();
                    selectedNodeValue = value;
                }
                break;
            }
        }
        if (!Objects.equals(selectedNodeValue, oldSelectedNodeValue)) {
            repaint();
        }
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if (selectedNodePos != null) {
            int mouseX = (int) mouseEvent.getX();
            int mouseY = (int) mouseEvent.getY();
            selectedNodePos.setX(delta.getX() + mouseX);
            selectedNodePos.setY(delta.getY() + mouseY);
            clampBounds(selectedNodePos);
            repaint();
        }
    }

    private void onMouseReleased(MouseEvent mouseEvent) {
        selectedNodePos = null;
    }

    private Point centerPoint() {
        int nodeWidth = nodeSize.getWidth();
        int nodeHeight = nodeSize.getHeight();
        return new Point((int) (canvas.getWidth() - nodeWidth) / 2,
                (int) (canvas.getHeight() - nodeHeight) / 2);
    }


    private boolean isLinksHasValue(Tree.Node<?> node, Object value) {
        return node.getParent() != null && value.equals(node.getParent().getValue())
                || (node.getLeft() != null && value.equals(node.getLeft().getValue())
                || (node.getRight() != null && value.equals(node.getRight().getValue())));
    }

    private void clampBounds(Point point) {
        Geometry.clamp(point,
                0, (int) (canvas.getWidth() - nodeSize.getWidth()),
                0, (int) (canvas.getHeight() - nodeSize.getHeight()));
    }

    private void repaint() {
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        context.setLineWidth(lineWidth);
        context.setFont(font);
        context.setStroke(Color.BLACK);

        int nodeWidth = nodeSize.getWidth();
        int nodeHeight = nodeSize.getHeight();
        int centerX = nodeWidth / 2;
        int centerY = nodeHeight / 2;

        tree.forEach(node -> {
            Point fromPos = pointMap.get(node.getValue());
            int x1 = fromPos.getX() + centerX;
            int y1 = fromPos.getY() + centerY;
            if (node.getLeft() != null) {
                Point toPos = pointMap.get(node.getLeft().getValue());
                int x2 = toPos.getX() + centerX;
                int y2 = toPos.getY() + centerY;
                context.strokeLine(x1, y1, x2, y2);
            }
            if (node.getRight() != null) {
                Point toPos = pointMap.get(node.getRight().getValue());
                int x2 = toPos.getX() + centerX;
                int y2 = toPos.getY() + centerY;
                context.strokeLine(x1, y1, x2, y2);
            }
        });

        Color nodeColor = nodeColorColorPicker.getValue();
        Color selectedColor = selectedColorPicker.getValue();
        Color linksColor = linksColorPicker.getValue();
        double nodeOffsetX = nodeSize.getWidth() / 2.3;
        double nodeOffsetY = nodeSize.getHeight() / 2.0;
        tree.forEach(node -> {
            Point nodePos = pointMap.get(node.getValue());
            int x = nodePos.getX();
            int y = nodePos.getY();
            Color color = nodeColor;
            if (selectedNodeValue != null) {
                if (selectedNodeValue.equals(node.getValue())) {
                    color = selectedColor;
                } else if (isLinksHasValue(node, selectedNodeValue)) {
                    color = linksColor;
                }
            }
            context.setFill(color);
            context.fillRect(x, y, nodeWidth, nodeHeight);

            context.setFill(Color.BLACK);
            context.strokeRect(x, y, nodeWidth, nodeHeight);
            context.fillText(Integer.toString(node.getValue()), x + nodeOffsetX, y + nodeOffsetY);
        });
    }
}