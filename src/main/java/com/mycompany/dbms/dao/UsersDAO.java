/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dbms.dao;

import com.mycompany.dbms.config.DBConnectionConfigs;
import com.mycompany.dbms.data.Addfunds;
import com.mycompany.dbms.data.Addsales;
import com.mycompany.dbms.data.Empdata;
import com.mycompany.dbms.data.Employee;
import com.mycompany.dbms.data.EmployeeProj;
import com.mycompany.dbms.data.ExtraExpenseAdd;
import com.mycompany.dbms.data.Project;
import com.mycompany.dbms.data.Userdata;
import com.sun.source.tree.BreakTree;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Query;

public class UsersDAO implements DAOInterface {

    private static UsersDAO instance;

    private UsersDAO() {

    }

    public static synchronized UsersDAO getInstance() {
        if (instance == null) {
            instance = new UsersDAO();

        }

        return instance;
    }

    /*
    public void save(Userdata user) {
        Connection connection = DBConnectionConfigs.getConnection();
        try {
            PreparedStatement pr = connection.prepareStatement("insert into trial (name, email, age) values (? , ?, ?) ");
            pr.setString(2, user.getEmail());
            pr.setString(1, user.getName());
            pr.setString(3, user.getAge());
            pr.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
     */
    @Override
    public Map<String, String> getdata(String username, String password) {
        Map<String, String> data = new HashMap<>();
        List<Map<String, String>> projects = new ArrayList<>();

        try (Connection connection = DBConnectionConfigs.getConnection(); 
                PreparedStatement pr = connection.prepareStatement("SELECT * FROM employees WHERE Username=? AND Password=?")) {

            pr.setString(1, username);
            pr.setString(2, password);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    data.put("EmployeeID", rs.getString("EmployeeID"));
                    data.put("EmployeeName", rs.getString("EmployeeName"));
                    data.put("EmployeeRole", rs.getString("EmployeeRole"));
                    data.put("PhoneNumber", rs.getString("PhoneNumber"));
                    data.put("Username", rs.getString("Username"));
                    data.put("Salary", rs.getString("Salary"));

                    // Call the stored procedure to calculate salary split
                    String employee = rs.getString("Username");
                    Map<String, String> salarySplit = calculateSalarySplit(employee);
                    if (salarySplit != null) {
                        System.out.println("abc");
                        data.putAll(salarySplit);

                        System.out.println(data);

                    } else {
                        System.out.println("Failed to calculate salary split.");
                    }

                } else {
                    System.out.println("Authentication failed. Please try again.");
                    data = null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
            data = null;
        }
        return data;
    }

    @Override
    public Map<String, String> calculateSalarySplit(String empId) {
        Map<String, String> salarySplit = new HashMap<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); CallableStatement stmt = connection.prepareCall("{CALL CalculateSalarySplitByUsername(?)}")) {
            stmt.setString(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    salarySplit.put("BasicSalary", rs.getString("BasicSalary"));
                    salarySplit.put("HouseRentAllowance", rs.getString("HouseRentAllowance"));
                    salarySplit.put("SpecialAllowance", rs.getString("SpecialAllowance"));
                    salarySplit.put("Bonus", rs.getString("Bonus"));
                    salarySplit.put("IncomeTax", rs.getString("IncomeTax"));
                    salarySplit.put("ProvidentFund", rs.getString("ProvidentFund"));
                    salarySplit.put("NetSalary", rs.getString("NetSalary"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salarySplit;
    }

    @Override
    public List<Map<String, String>> checkProject(String empUsername) {
        List<Map<String, String>> projectsList = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); CallableStatement stmt = connection.prepareCall("{CALL GetEmployeeProjectDetails(?)}")) {
            stmt.setString(1, empUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> projectDetails = new HashMap<>();
                    String projectID = rs.getString("ProjectID");
                    String projectName = rs.getString("ProjectName");
                    String timePeriod = rs.getString("TimePeriod");
                    String status = rs.getString("Status");
                    String expenses = rs.getString("Expenses");
                    String description = rs.getString("Description");
                    String teamMembersDetails = rs.getString("TeamMembersDetails");
                    projectDetails.put("ProjectID", projectID);
                    projectDetails.put("ProjectName", projectName);
                    projectDetails.put("TimePeriod", timePeriod);
                    projectDetails.put("Status", status);
                    projectDetails.put("Expenses", expenses);
                    projectDetails.put("Description", description);
                    projectDetails.put("TeamMembersDetails", teamMembersDetails);

                    projectsList.add(projectDetails);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Handle the exception appropriately
            projectsList = null;
        }
        return projectsList;
    }

    @Override
    public Map<String, String> admindata(String username, String password) {
        Map<String, String> dat = new HashMap<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM employees WHERE Username=? AND Password=? AND EmployeeRole = ?")) {
            pr.setString(1, username);
            pr.setString(2, password);
            pr.setString(3, "Admin");

            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    dat.put("EmployeeID", rs.getString("EmployeeID"));
                    dat.put("EmployeeName", rs.getString("EmployeeName"));
                    dat.put("EmployeeRole", rs.getString("EmployeeRole"));
                    dat.put("PhoneNumber", rs.getString("PhoneNumber"));
                    dat.put("Username", rs.getString("Username"));
                    dat.put("Salary", rs.getString("Salary"));
                    // Authentication succeeded
                    return dat;
                } else {
                    System.out.println("Authentication failed. Please try again.");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred during authentication", ex);
        }
        // Authentication failed
        return null;
    }

    @Override
    public int save(Empdata user) {
        Connection connection = DBConnectionConfigs.getConnection();
        try {
            // Check if the EmployeeID or Username already exists
            PreparedStatement checkStmt = connection.prepareStatement("SELECT EmployeeID, Username FROM employees WHERE EmployeeID = ? OR Username = ?");
            checkStmt.setInt(1, user.getEmployeeID());
            checkStmt.setString(2, user.getUsername());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return (0);
                //throw new RuntimeException("EmployeeID or Username already exists.");
            } else {
                PreparedStatement pr = connection.prepareStatement("INSERT INTO employees (EmployeeID, EmployeeName, EmployeeRole, PhoneNumber, Salary, Username, Password) VALUES (?, ?, ?, ?, ?, ?, ?)");
                pr.setInt(1, user.getEmployeeID());
                pr.setString(2, user.getEmployeeName());
                pr.setString(3, user.getEmployeeRole());
                pr.setString(4, user.getPhoneNumber());
                pr.setFloat(5, user.getSalary());
                pr.setString(6, user.getUsername());
                pr.setString(7, user.getPassword());
                pr.executeUpdate();
                return (1);
            }
        } catch (SQLException ex) {

            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);

        }
        return (0);
    }

    @Override
    public String updateProjectStatus(String projectID, String newStatus) {
        String resultMessage = "";
        Connection connection = null;
        try {
            connection = DBConnectionConfigs.getConnection();
            CallableStatement statement = connection.prepareCall("{? = CALL UpdateProjectStatus(?, ?)}");
            statement.registerOutParameter(1, Types.VARCHAR); // Register the return type
            statement.setString(2, projectID);
            statement.setString(3, newStatus);
            statement.execute();
            resultMessage = statement.getString(1); // Get the result from the first parameter
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception as needed
        } finally {
            // Close the connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultMessage;
    }

    @Override
    public int addextra(ExtraExpenseAdd expense) {
        Connection connection = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;
        try {
            connection = DBConnectionConfigs.getConnection();

            // Check if the Expense already exists based on Purpose and Amount
            PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM extraexpenses WHERE Purpose = ? AND Amount = ?");
            checkStmt.setString(1, expense.getPurpose());
            checkStmt.setInt(2, expense.getAmount());
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                return 0; // Expense already exists
            } else {
                insertStmt = connection.prepareStatement("INSERT INTO extraexpenses (Purpose, Amount) VALUES (?, ?)");
                insertStmt.setString(1, expense.getPurpose());
                insertStmt.setInt(2, expense.getAmount());
                insertStmt.executeUpdate();
                return 1; // Successfully inserted
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An SQL exception occurred", ex);
            return 0; // Error occurred
        } finally {
            // Close all resources in a finally block
            try {
                if (rs != null) {
                    rs.close();
                }
                if (insertStmt != null) {
                    insertStmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An SQL exception occurred while closing resources", ex);
            }
        }
    }

    @Override
    public int addextra(Addfunds addFunds) {
        Connection connection = null;
        PreparedStatement insertStmt = null;
        try {
            connection = DBConnectionConfigs.getConnection();

            insertStmt = connection.prepareStatement("INSERT INTO Funds (TransactionID, InvestorName, Amount) VALUES (?, ?, ?)");
            insertStmt.setString(1, addFunds.getTransactionID());
            insertStmt.setString(2, addFunds.getInvestorName());
            insertStmt.setInt(3, addFunds.getAmount());
            insertStmt.executeUpdate();

            return 1; // Successfully inserted
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
            return 0; // Error occurred
        } finally {
            // Close all resources in a finally block
            try {
                if (insertStmt != null) {
                    insertStmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean isSaleIDExists(String saleID) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        boolean exists = false;
        try {
            connection = DBConnectionConfigs.getConnection();
            String query = "SELECT COUNT(*) FROM sales WHERE SaleID = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, saleID);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                exists = (count > 0);
            }
        } catch (SQLException ex) {
            // Handle any SQL exceptions
            ex.printStackTrace();
        } finally {
            // Close resources in a finally block
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return exists;
    }

    @Override
    public int addNewSale(Addsales sale) {
        Connection connection = null;
        PreparedStatement insertStmt = null;
        try {
            connection = DBConnectionConfigs.getConnection();

            insertStmt = connection.prepareStatement("INSERT INTO sales (SaleID, ProjectID, Profit) VALUES (?, ?, ?)");
            insertStmt.setString(1, sale.getSaleID());
            insertStmt.setString(2, sale.getProjectID());
            insertStmt.setInt(3, sale.getProfit());
            insertStmt.executeUpdate();

            return 1; // Successfully inserted
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
            return 0; // Error occurred
        } finally {
            // Close all resources in a finally block
            try {
                if (insertStmt != null) {
                    insertStmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private static final String UPDATE_EMPLOYEE_PROCEDURE = "{CALL UpdateEmployee(?, ?, ?, ?)}";

    @Override
    public void updateEmployee(int empID, String empRole, String phone, double salary) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DBConnectionConfigs.getConnection();
            statement = connection.prepareCall(UPDATE_EMPLOYEE_PROCEDURE);
            statement.setInt(1, empID);
            statement.setString(2, empRole);
            statement.setString(3, phone);
            statement.setDouble(4, salary);
            statement.executeUpdate();
            System.out.println(phone);
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public List<Map<String, String>> getAllEmployees() {
        List<Map<String, String>> employeesData = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection();
                PreparedStatement pr = connection.prepareStatement("SELECT * FROM employees where EmployeeRole != 'Admin'")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("EmployeeID", rs.getString("EmployeeID"));
                    dat.put("EmployeeName", rs.getString("EmployeeName"));
                    dat.put("EmployeeRole", rs.getString("EmployeeRole"));
                    dat.put("PhoneNumber", rs.getString("PhoneNumber"));
                    dat.put("Username", rs.getString("Username"));
                    dat.put("Salary", rs.getString("Salary"));
                    employeesData.add(dat);

                }

                return employeesData;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    @Override
    public void deleteEmployee(int employeeID) {
        try (Connection connection = DBConnectionConfigs.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM projectemployees WHERE EmployeeID = ?")) {
                statement.setInt(1, employeeID);
                statement.executeUpdate();
            } catch (SQLException ex) {
                connection.rollback(); // Rollback transaction if an error occurs
                Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while deleting employee's projects", ex);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM employees WHERE EmployeeID = ? and EmployeeRole!=?")) {
                statement.setInt(1, employeeID);
                statement.setString(2, "Admin");
                // Setting int instead of String
                statement.executeUpdate();
            } catch (SQLException ex) {
                connection.rollback(); // Rollback transaction if an error occurs
                Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while deleting employee", ex);
                return;
            }

            // Commit the transaction if all operations succeed
            connection.commit();
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while managing transaction", ex);
        }
    }

    @Override
    public int saveEmployees(String projectId, List<EmployeeProj> employees) {
        int rowsAffected = 0;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBConnectionConfigs.getConnection();
            for (EmployeeProj employee : employees) {
                String query = "INSERT INTO projectemployees (ProjectID, EmployeeID) VALUES (?, ?)";
                statement = connection.prepareStatement(query);
                statement.setString(1, projectId);
                statement.setString(2, employee.getID());
                rowsAffected += statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }
        return rowsAffected;
    }

    @Override
    public int saveproj(Project project) {
        Connection connection = null;
        PreparedStatement projectStmt = null;
        try {
            connection = DBConnectionConfigs.getConnection();
            String projectQuery = "INSERT INTO Projects (Pname, timeperiod, status, expenses, description) VALUES ( ?, ?, ?, ?, ?)";
            projectStmt = connection.prepareStatement(projectQuery);
            projectStmt.setString(1, project.getPname());
            projectStmt.setFloat(2, project.getTimeperiod());
            projectStmt.setString(3, project.getStatus());
            projectStmt.setFloat(4, project.getExpenses());
            projectStmt.setString(5, project.getDescription());

            // Debugging: Print project query and parameters
            System.out.println("Project Query: " + projectQuery);
            System.out.println("Project Parameters: " + project.getPname() + ", " + project.getTimeperiod() + ", " + project.getStatus() + ", " + project.getExpenses() + ", " + project.getDescription());

            projectStmt.executeUpdate();
            return 1; // Success

        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
            return 0; // Failure

        } catch (Exception ex) {
            Logger.getLogger(UsersDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
            return 0; // Failure
        } finally {
            try {
                if (projectStmt != null) {
                    projectStmt.close();
                }
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException ex) {
                Logger.getLogger(UsersDAO.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public List<Map<String, String>> expenseEmployees() {
        List<Map<String, String>> employeesData = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM employees")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("EmployeeID", rs.getString("EmployeeID"));
                    dat.put("EmployeeName", rs.getString("EmployeeName"));
                    dat.put("Salary", rs.getString("Salary"));
                    employeesData.add(dat);

                }
                return employeesData;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    @Override
    public List<Map<String, String>> expenseExtra() {
        List<Map<String, String>> expenseData = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM extraexpenses")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("ExpenseID", rs.getString("ExpenseID"));
                    dat.put("Purpose", rs.getString("Purpose"));
                    dat.put("Amount", rs.getString("Amount"));
                    expenseData.add(dat);

                }
                System.out.println(expenseData);
                return expenseData;

            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    @Override
    public List<Map<String, String>> expenseProjects() {
        List<Map<String, String>> projects = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM projects")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("PID", rs.getString("PID"));
                    dat.put("Pname", rs.getString("Pname"));
                    dat.put("expenses", rs.getString("expenses"));
                    projects.add(dat);

                }
                return projects;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    @Override
    public List<Map<String, String>> expensesales() {
        List<Map<String, String>> sales = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM sales")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("SaleID", rs.getString("SaleID"));
                    dat.put("profit", rs.getString("profit"));
                    sales.add(dat);

                }
                return sales;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    @Override
    public List<Map<String, String>> expensefunds() {
        List<Map<String, String>> funds = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM funds")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("TransactionID", rs.getString("TransactionID"));
                    dat.put("InvestorName", rs.getString("InvestorName"));
                    dat.put("Amount", rs.getString("Amount"));
                    funds.add(dat);

                }
                return funds;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    public static Map<String, Double> executeAndStoreSubtotals() {
        Map<String, Double> subtotals = new HashMap<>();
        Connection connection = DBConnectionConfigs.getConnection();

        try {
            // Execute each SQL query and store the results
            String[] tables = {"employee", "project", "extra", "sales", "marketing", "funds"};
            for (String table : tables) {
                String query = "SELECT subtotal(?)";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, table);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double subtotal = rs.getDouble(1);
                            subtotals.put(table, subtotal);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing SQL queries: " + e.getMessage());
        }

        return subtotals;
    }

    @Override
    public List<Map<String, String>> expensemarketing() {
        List<Map<String, String>> market = new ArrayList<>();
        try (Connection connection = DBConnectionConfigs.getConnection(); PreparedStatement pr = connection.prepareStatement("SELECT * FROM marketing")) {

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> dat = new HashMap<>();
                    dat.put("MarketingID", rs.getString("MarketingID"));
                    dat.put("Name", rs.getString("Name"));
                    dat.put("Amount", rs.getString("Amount"));
                    market.add(dat);

                }
                return market;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersDAO.class.getName()).log(Level.SEVERE, "An error occurred while fetching employees data", ex);
        }
        return null;
    }

    @Override
    public String calculateNetProfitLoss() {
        try (Connection connection = DBConnectionConfigs.getConnection(); CallableStatement cs = connection.prepareCall("{? = call CalculateNetProfitLoss()}")) {

            cs.registerOutParameter(1, java.sql.Types.VARCHAR);

            cs.execute();

            return cs.getString(1);

        } catch (SQLException ex) {
            ex.printStackTrace();
            return "An error occurred while calculating net profit/loss.";
        }
    }
}
