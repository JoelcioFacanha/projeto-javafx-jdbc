package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DBException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller obj) {

		PreparedStatement st = null;

		try {

			st = conn.prepareStatement("INSERT INTO seller " + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "VALUES (?, ?, ?, ?, ?) ", Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected > 0) {

				ResultSet rs = st.getGeneratedKeys();

				if (rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}

				DB.closeResultSet(rs);

			} else {

				throw new DBException("Unexpected erro! No rows affected!");
			}

		} catch (SQLException e) {

			throw new DBException(e.getMessage());

		} finally {

			DB.closeStatement(st);

		}

	}

	@Override
	public void update(Seller obj) {

		PreparedStatement st = null;

		try {

			st = conn.prepareStatement(" UPDATE seller "
					+ " SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ?" + " WHERE Id = ?");

			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			st.setInt(6, obj.getId());

			st.executeUpdate();

		} catch (SQLException e) {

			throw new DBException(e.getMessage());

		} finally {

			DB.closeStatement(st);

		}

	}

	@Override
	public void deleteById(Integer id) {

		PreparedStatement st = null;

		try {

			st = conn.prepareStatement(" DELETE FROM seller WHERE Id = ?");
			st.setInt(1, id);

			st.executeUpdate();

		} catch (SQLException e) {

			throw new DBException(e.getMessage());

		} finally {

			DB.closeStatement(st);

		}
	}

	@Override
	public Seller findById(Integer id) {

		PreparedStatement st = null;
		ResultSet rs = null;

		try {

			st = conn.prepareStatement(
					"SELECT seller.Id, seller.Name, seller.Email, seller.BirthDate, seller.BaseSalary, seller.DepartmentId, department.Name as DepName"
							+ " FROM seller" + " INNER JOIN department ON seller.DepartmentId = department.Id"
							+ " WHERE seller.Id = ?");

			st.setInt(1, id);
			rs = st.executeQuery();

			if (rs.next()) {

				Department dept = instantiateDepartment(rs);
				return instantiateSeller(rs, dept);
			}

			return null;

		} catch (SQLException e) {

			throw new DBException(e.getMessage());

		} finally {

			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private Seller instantiateSeller(ResultSet rs, Department dept) throws SQLException {
		return new Seller(rs.getInt("Id"), rs.getString("Name"), rs.getString("Email"), rs.getDate("BirthDate"),
				rs.getDouble("BaseSalary"), dept);
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		return new Department(rs.getInt("DepartmentId"), rs.getString("DepName"));
	}

	@Override
	public List<Seller> findAll() {

		PreparedStatement st = null;
		ResultSet rs = null;

		try {

			st = conn.prepareStatement(
					"SELECT seller.Id, seller.Name, seller.Email, seller.BirthDate, seller.BaseSalary, seller.DepartmentId, department.Name as DepName"
							+ " FROM seller" + " INNER JOIN department ON seller.DepartmentId = department.Id"
							+ " ORDER BY seller.Name");

			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {

				Department dept = map.get(rs.getInt("DepartmentId"));

				if (dept == null) {

					dept = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dept);
				}

				Seller obj = instantiateSeller(rs, dept);
				list.add(obj);
			}

			return list;

		} catch (SQLException e) {

			throw new DBException(e.getMessage());

		} finally {

			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {

		PreparedStatement st = null;
		ResultSet rs = null;

		try {

			st = conn.prepareStatement(
					"SELECT seller.Id, seller.Name, seller.Email, seller.BirthDate, seller.BaseSalary, seller.DepartmentId, department.Name as DepName"
							+ " FROM seller" + " INNER JOIN department ON seller.DepartmentId = department.Id"
							+ " WHERE department.Id = ?" + " ORDER BY seller.Name");

			st.setInt(1, department.getId());
			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {

				Department dept = map.get(rs.getInt("DepartmentId"));

				if (dept == null) {

					dept = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dept);
				}

				Seller obj = instantiateSeller(rs, dept);
				list.add(obj);
			}

			return list;

		} catch (SQLException e) {

			throw new DBException(e.getMessage());

		} finally {

			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}
