package ru.track.prefork;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO implements AutoCloseable {
    private Connection[] connections;

    DAO () {
        connections = new Connection[3];
        connections[0] = getConnection(1);
        connections[1] = getConnection(2);
        connections[2] = getConnection(3);
    }

    public void close() {
        if (connections[0] != null) {
            try {
                connections[0].close();
            } catch (SQLException e) {}
        }
        if (connections[1] != null) {
            try {
                connections[1].close();
            } catch (SQLException e) {}
        }
        if (connections[2] != null) {
            try {
                connections[2].close();
            } catch (SQLException e) {}
        }
    }

    public static Connection getConnection(int domain) {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" +        //db type
                    "tdb-" + domain + ".trail5.net:" +    //host name
                    "3306/" +                //port
                    "track17?" +             //db name
                    "user=track_student&" +  //login
                    "password=7EsH.H6x";
            return DriverManager.getConnection(url    //password
            );
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long store(Message msg) {
        char firstLetter = msg.author.toLowerCase().charAt(0);
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        long result = -1;
        if (firstLetter >= 'a' && firstLetter <= 'j') {
            con = connections[0];
        } else if (firstLetter >= 'k' && firstLetter <= 't') {
            con = connections[1];
        } else if (firstLetter >= 'u' && firstLetter <= 'z') {
            con = connections[2];
        }
        if (con == null) {
            return -1;
        }
        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(
                    "INSERT INTO messages (user_name, text, ts) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, msg.author);
            stmt.setString(2, msg.data);
            Timestamp timestamp = new Timestamp(msg.ts);
            stmt.setTimestamp(3, timestamp);
            stmt.executeUpdate();
            con.commit();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {}
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {}
            }
        }
        return result;
    }

    List<Message> getHistory(long from, long to, long limit) {
        List<Message> history = new ArrayList<>();
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        for (int i = 0; i < 3; i++) {
            con = connections[i];
            if (con != null) {
                try {
                    stmt = con.prepareStatement(
                            "SELECT * FROM messages WHERE ts BETWEEN ? AND ? ORDER BY ts LIMIT ?");
                    Timestamp tsFrom = new Timestamp(from);
                    Timestamp tsTo = new Timestamp(to);
                    stmt.setTimestamp(1, tsFrom);
                    stmt.setTimestamp(2, tsTo);
                    stmt.setLong(3, limit);
                    stmt.executeQuery();
                    rs = stmt.getResultSet();
                    while (rs.next()) {
                        Date time = new Date(rs.getTimestamp("ts").getTime());
                        history.add(new Message(
                                time.getTime(),
                                rs.getString("text"),
                                rs.getString("user_name"))
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException ex) {}
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException ex) {}
                    }
                }
            }
        }
        history.sort(((Message o1, Message o2) -> {
            if (o1.ts > o2.ts) {
                return 1;
            } else if (o1.ts < o2.ts) {
                return -1;
            }
            return 0;
        }));
        if (history.size() > limit) {
            return history.subList(0, (int) limit);
        }
        return history;
    }

    List<Message> getByUser(String username, long limit) {
        List<Message> history = new ArrayList<>();
        char firstLetter = username.toLowerCase().charAt(0);
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        if (firstLetter >= 'a' && firstLetter <= 'j') {
            con = connections[0];
        } else if (firstLetter >= 'k' && firstLetter <= 't') {
            con = connections[1];
        } else if (firstLetter >= 'u' && firstLetter <= 'z') {
            con = connections[2];
        }
        if (con == null) {
            return history;
        }
        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(
                    "SELECT * FROM messages WHERE user_name = ? ORDER BY ts LIMIT ?");
            stmt.setString(1, username);
            stmt.setLong(2, limit);
            stmt.setQueryTimeout(10);
            stmt.executeQuery();
            rs = stmt.getResultSet();
            while (rs.next()) {
                Date time = new Date(rs.getTimestamp("ts").getTime());
                history.add(new Message(
                        time.getTime(),
                        rs.getString("text"),
                        rs.getString("user_name"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {}
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {}
            }
        }
        return history;
    }
}


