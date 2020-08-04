package gui;

import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DBException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;
	private SellerService service;
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;
	@FXML
	private TextField txtNome;
	@FXML
	private TextField txtEmail;
	@FXML
	private DatePicker dpBirthDate;
	@FXML
	private TextField txtBaseSalary;
	@FXML
	private Label lblErroNome;
	@FXML
	private Label lblErroEmail;
	@FXML
	private Label lblErroBirthDate;
	@FXML
	private Label lblErroBaseSalary;
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
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

			notifyDataChangeListeners();

			Utils.currentStage(event).close();

		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DBException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {

		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}

	}

	private Seller getFormData() {

		ValidationException exception = new ValidationException("Validation Exception!");

		if (txtNome.getText() == null || txtNome.getText().trim().equals("")) {
			exception.addError("Nome", " O campo nome não pode está vazio!");
		}

		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		// return new Seller(Utils.tryParseToInt(txtId.getText()), txtNome.getText());
		return new Seller();
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
		Constraints.setTextFieldMaxLength(txtNome, 100);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		Constraints.setTextFieldDouble(txtBaseSalary);
	}

	public void updateFormData() {

		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		txtId.setText(String.valueOf(entity.getId()));
		txtNome.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));

		if (entity.getBirthDate() != null) {
			dpBirthDate.setValue(entity.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
	}

	private void setErrorMessages(Map<String, String> errors) {

		Set<String> fields = errors.keySet();

		if (fields.contains("Nome")) {
			lblErroNome.setText(errors.get("Nome"));
		}
	}
}
