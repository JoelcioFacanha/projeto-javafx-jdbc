package gui;

import java.net.URL;
import java.util.ResourceBundle;

import db.DBException;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable {

	private Department entity;
	private DepartmentService service;

	@FXML
	private TextField txtId;
	@FXML
	private TextField txtNome;
	@FXML
	private Label lblErroNome;
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;

	public void setDepartment(Department entity) {
		this.entity = entity;
	}

	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {

		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		if (service == null) {
			throw new IllegalStateException("Service was null");
		}

		try {

			entity = getFormData();
			service.saveOrUpadate(entity);

			Utils.currentStage(event).close();

		} catch (DBException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private Department getFormData() {

		return new Department(Utils.tryParseToInt(txtId.getText()), txtNome.getText());
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		initializeNodes();
	}

	public void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtNome, 30);
	}

	public void updateFormDate() {

		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		txtId.setText(String.valueOf(entity.getId()));
		txtNome.setText(entity.getName());
	}
}