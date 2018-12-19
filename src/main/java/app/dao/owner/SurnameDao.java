package app.dao.owner;

import app.dao.ConnectionBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SurnameDao {

    private static final Logger logger = LogManager.getLogger(SurnameDao.class);

    private static final String INSERT_SURNAME = "insert into surnames(value) value (?)";
    private static final String SELECT_SURNAME = "select id from surnames where value = ?";

    /**
     * return id for surname in database or 0 if surname does not exist
     * @param surname
     * @return surname id. If surname does not exist return 0
     */
    public int getId(String surname) {
        logger.trace("");

        int id = 0;

        try (Connection conn = ConnectionBuilder.getConnection();
        PreparedStatement stmt = conn.prepareStatement(SELECT_SURNAME)){
           stmt.setString(1, surname);
           ResultSet rs = stmt.executeQuery();

           while (rs.next()) {
               id = rs.getInt("id");
           }
        }
        catch (SQLException ex) {
            logger.error(ex.getMessage());
        }

        return id;
    }

    /**
     * save surname to database
     * @param surname
     */
    public Long saveSurname(String surname) {
        logger.trace("");

        Long result = -1L;

        try (Connection con = ConnectionBuilder.getConnection();
             PreparedStatement stmt = con.prepareStatement(INSERT_SURNAME)) {

            con.setAutoCommit(false);

            try {
                stmt.setString(1, surname);
                stmt.executeUpdate();
                con.commit();
            }
            catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
        catch (SQLException ex) {
            logger.error(ex.getMessage());
        }

        return result;
    }

//    public static void main(String[] args) {
//
//        String val = "DUpl";
//        SurnameDao sn = new SurnameDao();
//
//        Long result = sn.saveSurname(val);
//        System.out.println(result);
//
//        System.out.println(val + " id = " + sn.getId(val));
//
//        val = "Petia";
//        System.out.println(val + " id = " + sn.getId(val));
//    }
}