package app.dao;

import app.dao.handbooks.device.*;
import app.exceptions.DeviceDaoException;
import app.models.Device;
import app.models.DeviceAndHisRepair;
import app.models.Repair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Collections;
import java.util.Vector;

//Data Access Object for device
public class DeviceDao {

	private static final Logger log = LogManager.getLogger(DeviceDao.class);

	private static final String tableName = "device";
	private Device device;

	public DeviceDao(Device device) {
		this.device = device;
	}

	public DeviceDao() {
		this.device = new Device();
	}

	/**
	 * insert to database information about owner
	 */
	public int insert() {
		log.trace("");

		int id = 0;
		int type_id = selectTypeId();
		int brand_id = selectBrandId();
		int model_id = selectModelId();
		String serialNumber = device.getSerialNumber();
		int defect_id = selectDefectId();
		int owner_id = device.getOwnerId();
		int repair_id = device.getRepairId();
		int completeness_id = selectCompletenessId();
		int appearance_id = selectAppearanceId();
		String note = device.getNote();

		String sql = "insert into " + tableName +
				"(type_id, brand_id, model_id, serial_number, defect_id," +
				" owner_id, repair_id, completeness_id, appearance_id, note) " +
				"values(?,?,?,?,?,?,?,?,?,?)";

		try (Connection co = ConnectionBuilder.getConnection();
			 PreparedStatement stmt = co.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, type_id);
			stmt.setInt(2, brand_id);
			stmt.setInt(3, model_id);
			stmt.setString(4, serialNumber);
			stmt.setInt(5, defect_id);
			stmt.setInt(6, owner_id);
			stmt.setInt(7, repair_id);
			stmt.setInt(8, completeness_id);
			stmt.setInt(9, appearance_id);
			stmt.setString(10, note);

			stmt.execute();

			//Retrieves any auto-generated keys created as a result of executing this Statement object.
			ResultSet rs = stmt.getGeneratedKeys();

			if (rs.next()) {
				id = rs.getInt(1);
			}

		}
		catch (SQLException ex) {
			log.error(ex.getMessage());
		}

		return id;
	}

	private int selectModelId() {
		String devModel = device.getModel();
		ModelDao modelDao = new ModelDao();
		int model_id = modelDao.selectId(devModel);

		if (model_id == 0) {
			model_id = modelDao.insert(devModel);
		}
		return model_id;
	}

	private int selectBrandId() {
		String devBrand = device.getBrand();
		BrandDao brandDao = new BrandDao();
		int brand_id = brandDao.selectId(devBrand);

		if (brand_id == 0) {
			brand_id = brandDao.insert(devBrand);
		}
		return brand_id;
	}

	private int selectTypeId() {
		String devType = device.getType();
		TypeDao typeDao = new TypeDao();
		int type_id = typeDao.selectId(devType);

		if (type_id == 0) {
			type_id = typeDao.insert(devType);
		}
		return type_id;
	}

	private int selectDefectId() {
		String defect = device.getDefect();
		DefectDao defectDao = new DefectDao();
		int defect_id = defectDao.selectId(defect);

		if (defect_id == 0) {
			defect_id = defectDao.insert(defect);
		}
		return defect_id;
	}

	private int selectCompletenessId() {
		String completeness = device.getCompleteness();
		CompletenessDao completenessDao = new CompletenessDao();
		int completness_id = completenessDao.selectId(completeness);

		if (completness_id == 0) {
			completness_id = completenessDao.insert(completeness);
		}
		return completness_id;
	}

	private int selectAppearanceId() {
		String appearance = device.getAppearance();
		AppearanceDao appearanceDao = new AppearanceDao();
		int appearance_id = appearanceDao.selectId(appearance);

		if (appearance_id == 0) {
			appearance_id = appearanceDao.insert(appearance);
		}
		return appearance_id;
	}

	public static int selectMaxId() {
		int maxId = 0;

		String sql = "Select max(id) as id from " + tableName;

		try (Connection conn = ConnectionBuilder.getConnection();
			 Statement stmt = conn.createStatement()) {

			ResultSet rs = stmt.executeQuery(sql);

			rs.next();

			maxId = rs.getInt("id");
		}
		catch (SQLException e) {
			log.error(e.getMessage());
		}
		return maxId;
	}

	/**
	 * @return list of devises with given status
	 */
	public static Vector<Device> select(String status) {
		log.trace("");

		final String select_by_status = "select device.id, type.value, brand.value, model.value, serial_number, " +
				"defect.value, owner_id, repair_id, status.value, completeness.value, appearance.value, note " +
				"from device " +
				"inner join type on device.type_id = type.id " +
				"inner join brand on device.brand_id = brand.id " +
				"inner join model on device.model_id = model.id " +
				"inner join defect on device.defect_id = defect.id " +
				"inner join completeness on device.completeness_id = completeness.id " +
				"inner join appearance on device.appearance_id = appearance.id " +
				"inner join repair on device.repair_id = repair.id " +
				"inner join status on repair.status_id = status.id " +
				"WHERE status.value = '" + status + "'";

		return makeRequest(select_by_status);
	}

	/**
	 * @return list of ids of devises with given status
	 */
	public static Vector<Integer> selectIdsByStatus(String status) {
		log.trace("");

        final String select_by_status = "select device.id, repair_id, status.value " +
                "from device " +
                "inner join repair on device.repair_id = repair.id " +
                "inner join status on repair.status_id = status.id " +
                "WHERE status.value = '" + status + "' ";

        Vector<Integer> listOfDevicesIds = new Vector<>();

        try (Connection co = ConnectionBuilder.getConnection();
             Statement st = co.createStatement()) {
            ResultSet result = st.executeQuery(select_by_status);
            while (result.next()) {
                listOfDevicesIds.add(result.getInt("id"));
            }
        }
        catch (SQLException sex) {
            log.error(sex.getMessage());
        }

        Collections.sort(listOfDevicesIds );
        return listOfDevicesIds ;
	}

	public static Vector<Device> selectByStatusAndMaster(String status, String masterLogin) {
		log.trace("");

		final String select_by_status = "select device.id, type.value, brand.value, model.value, serial_number, " +
				"defect.value, owner_id, repair_id, status.value, completeness.value, appearance.value, note " +
				"from device " +
				"inner join type on device.type_id = type.id " +
				"inner join brand on device.brand_id = brand.id " +
				"inner join model on device.model_id = model.id " +
				"inner join defect on device.defect_id = defect.id " +
				"inner join completeness on device.completeness_id = completeness.id " +
				"inner join appearance on device.appearance_id = appearance.id " +
				"inner join repair on device.repair_id = repair.id " +
				"inner join status on repair.status_id = status.id " +
				"inner join user on repair.master_id = user.id " +
				"WHERE status.value = '" + status + "' " +
				"and user.login = '" + masterLogin + "'";

		return makeRequest(select_by_status);
	}

	public static Vector<DeviceAndHisRepair> select(String status, String masterLogin) {
		log.trace("");

		final String select = "select " +
				"device.id," +
				"type.value, " +
				"brand.value, " +
				"model.value, " +
				"device.serial_number, " +
				"defect.value, " +
				"device.owner_id, " +
				"status.value, " +
				"completeness.value, " +
				"appearance.value, " +
				"device.note, " +
				"repair.id, " +
				"repair.date_of_receipt, " +
				"repair.master_comments, " +
				"repair.diagnostic_result, " +
				"repair.repair_result " +
				"from device " +
				"inner join type on device.type_id = type.id  " +
				"inner join brand on device.brand_id = brand.id " +
				"inner join model on device.model_id = model.id " +
				"inner join defect on device.defect_id = defect.id " +
				"inner join completeness on device.completeness_id = completeness.id " +
				"inner join appearance on device.appearance_id = appearance.id " +
				"inner join repair on device.repair_id = repair.id " +
				"inner join status on repair.status_id = status.id " +
				"inner join user on repair.master_id = user.id " +
				"WHERE status.value = '" + status + "' " +
				"and user.login = '" + masterLogin + "'";

		Vector<DeviceAndHisRepair> listOfDevices = new Vector<>();

		try (Connection co = ConnectionBuilder.getConnection();
			 Statement st = co.createStatement()) {

			ResultSet result = st.executeQuery(select);

			while (result.next()) {
				Device device = new Device();
				device.setId(result.getInt("id"));
				device.setType(result.getString("type.value"));
				device.setBrand(result.getString("brand.value"));
				device.setModel(result.getString("model.value"));
				device.setSerialNumber(result.getString("serial_number"));
				device.setDefect(result.getString("defect.value"));
				device.setOwnerId(result.getInt("owner_id"));
				device.setCompleteness(result.getString("completeness.value"));
				device.setAppearance(result.getString("appearance.value"));
				device.setNote(result.getString("note"));

				Repair repair = new Repair();
				repair.setId(result.getInt("repair.id"));
				repair.setDateOfAccept(result.getString("repair.date_of_receipt"));
				repair.setMasterComments(result.getString("repair.master_comments"));
				repair.setDiagnosticResult(result.getString("repair.diagnostic_result"));
				repair.setRepairResult(result.getString("repair.repair_result"));

				listOfDevices.add(new DeviceAndHisRepair(device, repair));
			}
		}
		catch (SQLException sex) {
			log.error(sex.getMessage());
		}

        Collections.sort(listOfDevices);
		return listOfDevices;
	}

	public static Vector<DeviceAndHisRepair> selectDeviceAndHisRepair(String status) {
		log.trace("");

		final String select = "select " +
				"device.id," +
				"type.value, " +
				"brand.value, " +
				"model.value, " +
				"device.serial_number, " +
				"defect.value, " +
				"device.owner_id, " +
				"status.value, " +
				"completeness.value, " +
				"appearance.value, " +
				"device.note, " +
				"repair.id, " +
				"repair.date_of_receipt, " +
				"repair.master_comments, " +
                "repair.diagnostic_result, " +
                "repair.repair_result " +
				"from device " +
				"inner join type on device.type_id = type.id  " +
				"inner join brand on device.brand_id = brand.id " +
				"inner join model on device.model_id = model.id " +
				"inner join defect on device.defect_id = defect.id " +
				"inner join completeness on device.completeness_id = completeness.id " +
				"inner join appearance on device.appearance_id = appearance.id " +
				"inner join repair on device.repair_id = repair.id " +
				"inner join status on repair.status_id = status.id " +
//				"inner join user on repair.master_id = user.id " +
				"WHERE status.value = '" + status + "' ";

		Vector<DeviceAndHisRepair> listOfDevices = new Vector<>();

		try (Connection co = ConnectionBuilder.getConnection();
			 Statement st = co.createStatement()) {
			ResultSet result = st.executeQuery(select);
			while (result.next()) {
				Device device = new Device();

				device.setId(result.getInt("id"));
				device.setType(result.getString("type.value"));
				device.setBrand(result.getString("brand.value"));
				device.setModel(result.getString("model.value"));
				device.setSerialNumber(result.getString("serial_number"));
				device.setDefect(result.getString("defect.value"));
				device.setOwnerId(result.getInt("owner_id"));
				device.setCompleteness(result.getString("completeness.value"));
				device.setAppearance(result.getString("appearance.value"));
				device.setNote(result.getString("note"));

				Repair repair = new Repair();
				repair.setId(result.getInt("repair.id"));
				repair.setDateOfAccept(result.getString("repair.date_of_receipt"));
				repair.setMasterComments(result.getString("repair.master_comments"));
				repair.setDiagnosticResult(result.getString("repair.diagnostic_result"));
				repair.setRepairResult(result.getString("repair.repair_result"));

				//todo do not select correct repair id

				listOfDevices.add(new DeviceAndHisRepair(device, repair));
			}
		}
		catch (SQLException sex) {
			log.error(sex.getMessage());
		}

        Collections.sort(listOfDevices);
		return listOfDevices;
	}

	private static Vector<Device> makeRequest(String select_by_status) {
		Vector<Device> listOfDevices = new Vector<>();

		try (Connection co = ConnectionBuilder.getConnection();
			 Statement st = co.createStatement()) {
			ResultSet result = st.executeQuery(select_by_status);
			while (result.next()) {
				Device device = new Device();

				device.setId(result.getInt("id"));
				device.setType(result.getString("type.value"));
				device.setBrand(result.getString("brand.value"));
				device.setModel(result.getString("model.value"));
				device.setSerialNumber(result.getString("serial_number"));
				device.setDefect(result.getString("defect.value"));
				device.setOwnerId(result.getInt("owner_id"));
				device.setRepairId(result.getInt("repair_id"));
				device.setCompleteness(result.getString("completeness.value"));
				device.setAppearance(result.getString("appearance.value"));
				device.setNote(result.getString("note"));

				listOfDevices.add(device);
			}
		}
		catch (SQLException sex) {
			log.error(sex.getMessage());
		}

		Collections.sort(listOfDevices);
		return listOfDevices;
	}

	/**
	 * select devices by date of receive
	 */
	public static Vector<Device> selectByDate(String date) {
		log.trace("");

		//
		final String select_by_date = "select device.id, type.value, brand.value, model.value, serial_number, " +
				"defect.value, owner_id, repair_id, status.value, completeness.value, appearance.value, note " +
				"from device " +
				"inner join type on device.type_id = type.id " +
				"inner join brand on device.brand_id = brand.id " +
				"inner join model on device.model_id = model.id " +
				"inner join defect on device.defect_id = defect.id " +
				"inner join completeness on device.completeness_id = completeness.id " +
				"inner join appearance on device.appearance_id = appearance.id " +
				"inner join repair on device.repair_id = repair.id " +
				"inner join status on repair.status_id = status.id " +
				"WHERE repair.date_of_receipt = '" + date + "'";

		return makeRequest(select_by_date);
	}

	public static DeviceAndHisRepair selectById(Integer id) throws DeviceDaoException {
		log.trace("");

		DeviceAndHisRepair deviceAndHisRepair = null;

		final String select_by_id = "select " +
				"device.id," +
				"type.value, " +
				"brand.value, " +
				"model.value, " +
				"device.serial_number, " +
				"defect.value, " +
				"device.owner_id, " +
				"status.value, " +
				"completeness.value, " +
				"appearance.value, " +
				"device.note, " +
				"repair.id, " +
				"repair.date_of_receipt, " +
				"repair.master_comments, " +
				"repair.diagnostic_result, " +
				"repair.repair_result " +
				"from device " +
				"inner join type on device.type_id = type.id  " +
				"inner join brand on device.brand_id = brand.id " +
				"inner join model on device.model_id = model.id " +
				"inner join defect on device.defect_id = defect.id " +
				"inner join completeness on device.completeness_id = completeness.id " +
				"inner join appearance on device.appearance_id = appearance.id " +
				"inner join repair on device.repair_id = repair.id " +
				"inner join status on repair.status_id = status.id " +
				"inner join user on repair.master_id = user.id " +
				"WHERE device.id = '" + id + "'";

		try (Connection co = ConnectionBuilder.getConnection();
			 Statement st = co.createStatement()) {

			ResultSet result = st.executeQuery(select_by_id);

			while (result.next()) {
				Device device = new Device();
				device.setId(result.getInt("id"));
				device.setType(result.getString("type.value"));
				device.setBrand(result.getString("brand.value"));
				device.setModel(result.getString("model.value"));
				device.setSerialNumber(result.getString("serial_number"));
				device.setDefect(result.getString("defect.value"));
				device.setOwnerId(result.getInt("owner_id"));
				device.setCompleteness(result.getString("completeness.value"));
				device.setAppearance(result.getString("appearance.value"));
				device.setNote(result.getString("note"));

				Repair repair = new Repair();
				repair.setId(result.getInt("repair.id"));
				repair.setDateOfAccept(result.getString("repair.date_of_receipt"));
				repair.setMasterComments(result.getString("repair.master_comments"));
				repair.setDiagnosticResult(result.getString("repair.diagnostic_result"));
				repair.setRepairResult(result.getString("repair.repair_result"));

				deviceAndHisRepair = new DeviceAndHisRepair(device, repair);
			}
		}
		catch (SQLException sex) {
			log.error(sex.getMessage());
			throw new DeviceDaoException("Something went wrong in DeviceDao.selectById() method");
		}

		return deviceAndHisRepair;
	}
}
