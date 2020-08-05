package gui;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;
	private SellerService service;
	private DepartmentService departmentService;
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
	private ComboBox<Department> cbDepartment;
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

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerService sellerService, DepartmentService departmentService) {
		this.service = sellerService;
		this.departmentService = departmentService;
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

		Seller seller = new Seller();

		ValidationException exception = new ValidationException("Validation Exception!");

		seller.setId(Utils.tryParseToInt(txtId.getText()));

		if (txtNome.getText() == null || txtNome.getText().trim().equals("")) {
			exception.addError("Nome", " O campo não pode ser vazio!");
		}

		seller.setName(txtNome.getText());

		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("Email", " O campo não pode ser vazio!");
		}

		seller.setEmail(txtEmail.getText());

		if (dpBirthDate.getValue() == null) {
			exception.addError("BirthDate", "O campo não pode ser vazio!");
		} else {
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			seller.setBirthDate(Date.from(instant));
		}

		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addError("BaseSalary", " O campo não pode ser vazio!");
		}

		seller.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));

		seller.setDepartment(cbDepartment.getValue());

		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return seller;
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

		initializeComboBoxDepartment();
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

		if (entity.getDepartment() == null) {
			cbDepartment.getSelectionModel().selectFirst();
		} else {
			cbDepartment.setValue(entity.getDepartment());
		}
	}

	private void setErrorMessages(Map<String, String> errors) {

		Set<String> fields = errors.keySet();

		lblErroNome.setText(fields.contains("Nome") ? errors.get("Nome") : "");
		lblErroEmail.setText(fields.contains("Email") ? errors.get("Email") : "");
		lblErroBaseSalary.setText(fields.contains("BaseSalary") ? errors.get("BaseSalary") : "");
		lblErroBirthDate.setText(fields.contains("BirthDate") ? errors.get("BirthDate") : "");
	}

	public void loadAssociatedObjects() {

		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService was null!");
		}

		obsList = FXCollections.observableArrayList(departmentService.findAll());
		cbDepartment.setItems(obsList);
	}

	private void initializeComboBoxDepartment() {

		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};

		cbDepartment.setCellFactory(factory);
		cbDepartment.setButtonCell(factory.call(null));
	}
}
