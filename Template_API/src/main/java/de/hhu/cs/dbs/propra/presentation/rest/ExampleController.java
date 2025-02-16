package de.hhu.cs.dbs.propra.presentation.rest;

import de.hhu.cs.dbs.propra.domain.model.User;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Path("/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class ExampleController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET // GET http://localhost:8080
    public String halloWelt() {
        return "Hallo Welt!";
    }

    @Path("foo")
    @RolesAllowed({"KUNDE", "PROJEKTLEITER", "ADMIN"})
    @GET
    // GET http://localhost:8080/foo => OK, wenn Benutzer die Rolle "USER", "EMPLOYEE" oder "ADMIN" hat. Siehe SQLiteUserRepository.
    public String halloFoo() {
        return "Hallo " + securityContext.getUserPrincipal() + "!";
    }

    @Path("foo2/{bar}")
    @GET // GET http://localhost:8080/foo/xyz
    public String halloFoo2(@PathParam("bar") String bar) {
        if (!bar.equals("foo")) throw new NotFoundException("Resource '" + bar + "' not found");
        return "Hallo " + bar + "!";
    }

    @Path("foo3")
    @GET // GET http://localhost:8080/foo3?bar=xyz
    public String halloFoo3(@QueryParam("bar") String bar) {
        return "Hallo " + bar + "!";
    }

    @Path("bar")
    @GET // GET http://localhost:8080/bar => Bad Request; http://localhost/bar?foo=xyz => OK
    public Response halloBar(@QueryParam("foo") String foo) {
        if (foo == null) throw new BadRequestException();
        return Response.status(Response.Status.OK).entity("Hallo Bar!").build();
    }

    @Path("bar2")
    @GET // GET http://localhost:8080/bar2
    public List<Map<String, Object>> halloBar2(@QueryParam("name") @DefaultValue("Max Mustermann") List<String> names) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT ?;");
        preparedStatement.closeOnCompletion();
        int random = ThreadLocalRandom.current().nextInt(0, names.size());
        preparedStatement.setObject(1, names.get(random));
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new HashMap<>();
            entity.put("name", resultSet.getObject(1));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities;
    }

    @Path("foo")
    @POST // POST http://localhost:8080/foo
    public Response einUpload(@FormDataParam("name") String name, @FormDataParam("file") InputStream file) {
        if (name == null) return Response.status(Response.Status.BAD_REQUEST).build();
        if (file == null) return Response.status(Response.Status.BAD_REQUEST).build();
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(file);
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path("234235").build()).build();
    }

 //*************************************************************************************************************

    private void addNutzer(String email, String password, Connection connection) throws SQLException {
        if ( getNutzer(email).isEmpty()) {
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO Nutzer VALUES(?,?);");
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, password);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();
        }
    }

    @Path("nutzer")
    @GET // GET http://localhost:8080/Nutzer
    public List<Map<String, Object>> getNutzer(@QueryParam("email") @DefaultValue("") String email) throws SQLException {
            try {
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement;
                String sql = "SELECT rowid, * FROM Nutzer ";

                if (!email.isEmpty()) {
                    sql = sql + "WHERE \"E-Mail-Adresse\" = ? ;";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setObject(1,email);
                }
                else {
                    sql = sql + ";";
                    preparedStatement = connection.prepareStatement(sql);
                }

                preparedStatement.closeOnCompletion();
                ResultSet resultSet = preparedStatement.executeQuery();
                List<Map<String, Object>> entities = new ArrayList<>();
                Map<String, Object> entity;
                while (resultSet.next()) {
                    entity = new LinkedHashMap<>();
                    entity.put("nutzerid", resultSet.getObject(1));
                    entity.put("email", resultSet.getObject(2));
                    entity.put("password", resultSet.getObject(3));
                    entities.add(entity);
                }
                resultSet.close();
                connection.close();
                return entities;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

            return null;
    }

    @Path("nutzer")
    @POST // GET http://localhost:8080/nutzer
    public Response postNutzer(@FormDataParam("email") String email,
                               @FormDataParam("password") String password ) throws SQLException {

            try {
                Connection connection = dataSource.getConnection();
                addNutzer(email, password, connection);
                connection.close();
                return Response.status(Response.Status.fromStatusCode(201))
                        .header("location",uriInfo.getAbsolutePath()+"/"+getNutzer(email).get(0).get("nutzerid").toString())
                        .build();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                var message = new HashMap<String,String>();
                message.put("message",e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).
                        entity(message).build();
            }

    }


 //*************************************************************************************************************

    @Path("kunden")
    @GET // GET http://localhost:8080/kunde
    public List<Map<String, Object>> getKunde(
            @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("telefonnummer") @DefaultValue("") String telefonnummer) throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement;
        String sql = "SELECT Nutzer.ROWID, Kunde.ROWID, Kunde.* ,Nutzer.Passwort " +
                "FROM Kunde, Nutzer WHERE Nutzer.\"E-Mail-Adresse\" = Kunde.\"E-Mail-Adresse\" ";

        if (!email.isEmpty() && !telefonnummer.isEmpty()){
            sql = sql + "AND Kunde.\"E-Mail-Adresse\" = ? AND Telefonnummer = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,email);
            preparedStatement.setObject(2,telefonnummer);
        }

        else if (!email.isEmpty()) {
            sql = sql + "AND Kunde.\"E-Mail-Adresse\" = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,email);
        }
        else if (!telefonnummer.isEmpty()) {
            sql = sql + "AND Telefonnummer = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,telefonnummer);
        }
        else {
            sql = sql + ";";
            preparedStatement = connection.prepareStatement(sql);
        }

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("nutzerid", resultSet.getObject(1));
            entity.put("kundenid", resultSet.getObject(2));
            entity.put("email", resultSet.getObject(3));
            entity.put("password", resultSet.getObject(5));
            entity.put("telefon", resultSet.getObject(4));

            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities;
    }

    @Path("kunden")
    @POST // GET http://localhost:8080/nutzer
    public Response postKunde(@FormDataParam("email") String email,
                               @FormDataParam("password") String password,
                               @FormDataParam("telefonnummer") String telefonnummer ) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            addNutzer(email,password,connection);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO Kunde VALUES( ? , ? );");
            preparedStatement.setObject(1,email);
            preparedStatement.setObject(2,telefonnummer);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();
            connection.commit();
            return Response.status(Response.Status.fromStatusCode(201))
                    .header("location",uriInfo.getAbsolutePath()+"/"+getKunde("",telefonnummer).get(0).get("kundenid").toString())
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }


    }


//*************************************************************************************************************
    @Path("projektleiter")
    @GET // GET http://localhost:8080/kunde
    public List<Map<String, Object>> getProjektleiter (
            @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("gehalt") @DefaultValue("") String gehalt) throws SQLException {


        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement;
        String sql = "SELECT Nutzer.ROWID, Projektleiter.ROWID, Projektleiter.* ,Nutzer.Passwort " +
                "FROM Projektleiter, Nutzer WHERE Nutzer.\"E-Mail-Adresse\" = Projektleiter.\"E-Mail-Adresse\" ";

        if (!email.isEmpty() && !gehalt.isEmpty()){
            sql = sql + "AND Projektleiter.\"E-Mail-Adresse\" = ? AND Gehalt > ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,email);
            preparedStatement.setObject(2,gehalt);
        }

        else if (!email.isEmpty()) {
            sql = sql + "AND Projektleiter.\"E-Mail-Adresse\" = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,email);
        }
        else if (!gehalt.isEmpty()) {
            sql = sql + "AND Gehalt > ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,gehalt);
        }
        else {
            sql = sql + ";";
            preparedStatement = connection.prepareStatement(sql);
        }

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("nutzerid", resultSet.getObject(1));
            entity.put("projektleiterid", resultSet.getObject(2));
            entity.put("email", resultSet.getObject(3));
            entity.put("password", resultSet.getObject(5));
            entity.put("gehalt", resultSet.getObject(4));

            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities;
    }

    @Path("projektleiter")
    @POST // GET http://localhost:8080/nutzer
    public Response postProjektleiter(@FormDataParam("email") String email,
                              @FormDataParam("password") String password,
                              @FormDataParam("gehalt") String gehalt ) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            addNutzer(email,password,connection);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO Projektleiter VALUES( ? , ? );");
            preparedStatement.setObject(1,email);
            preparedStatement.setObject(2,gehalt);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();
            connection.commit();
            return Response.status(Response.Status.fromStatusCode(201))
                    .header("location",uriInfo.getAbsolutePath()+"/"+getProjektleiter(email,"").get(0).get("projektleiterid").toString())
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }

    }

//*************************************************************************************************************

    private void addSpezialist(String email, String password, String verfuegbarkeitsstatus, Connection connection) throws SQLException {
        if ( getSpezialisten(email,"").isEmpty()) {
            connection.setAutoCommit(false);
            addNutzer(email, password, connection);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO Spezialist VALUES( ? , ? );");
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, verfuegbarkeitsstatus);
            preparedStatement.executeUpdate();
        }
    }

    @Path("spezialisten")
    @GET // GET http://localhost:8080/kunde
    public List<Map<String, Object>> getSpezialisten (
            @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("verfuegbar") @DefaultValue("") String verfuegbar) throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement;
        String sql = "SELECT Nutzer.ROWID, Spezialist.ROWID, Spezialist.* ,Nutzer.Passwort " +
                "FROM Spezialist, Nutzer WHERE Nutzer.\"E-Mail-Adresse\" = Spezialist.\"E-Mail-Adresse\" ";

        if (!email.isEmpty() && !verfuegbar.isEmpty()){
            sql = sql + "AND Spezialist.\"E-Mail-Adresse\" = ? AND Verfugbarkeitsstatus = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,email);
            preparedStatement.setObject(2,verfuegbar);
        }

        else if (!email.isEmpty()) {
            sql = sql + "AND Spezialist.\"E-Mail-Adresse\" = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,email);
        }
        else if (!verfuegbar.isEmpty()) {
            sql = sql + "AND Verfugbarkeitsstatus = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,verfuegbar);
        }
        else {
            sql = sql + ";";
            preparedStatement = connection.prepareStatement(sql);
        }

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("nutzerid", resultSet.getObject(1));
            entity.put("spezialistid", resultSet.getObject(2));
            entity.put("email", resultSet.getObject(3));
            entity.put("password", resultSet.getObject(5));
            entity.put("verfuegbarkeitsstatus", resultSet.getObject(4));

            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities;
    }


    @Path("spezialisten")
    @POST // GET http://localhost:8080/spezialisten
    public Response postSpezialisten(@FormDataParam("email") String email,
                                      @FormDataParam("password") String password,
                                      @FormDataParam("verfuegbarkeitsstatus") String verfuegbarkeitsstatus ) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            addSpezialist(email, password, verfuegbarkeitsstatus, connection);
            connection.commit();
            return Response.status(Response.Status.fromStatusCode(201))
                    .header("location",uriInfo.getAbsolutePath()+"/"+getSpezialisten(email,"").get(0).get("spezialistid").toString())
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }

    }

//*************************************************************************************************************

    private void addProjekt(String email, String password, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("INSERT INTO Nutzer VALUES(?,?);");
        preparedStatement.closeOnCompletion();
        preparedStatement.setObject(1, email);
        preparedStatement.setObject(2, password);
        preparedStatement.executeUpdate();
    }

    @Path("projekte")
    @GET // GET http://localhost:8080/projekte
    public List<Map<String, Object>> getProjekte() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement;
            String sql = "SELECT * FROM Projekt;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("projektid", resultSet.getObject(1));
                entity.put("name", resultSet.getObject(2));
                entity.put("deadline", resultSet.getObject(3));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return entities;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    @Path("projekte/{projektid}/bewertungen")
    @GET // GET http://localhost:8080/projekte/{projektid}/bewertungen
    public List<Map<String, Object>> getBewertungen(@PathParam("projektid") String projektid ) throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement;

            String sql ="SELECT\n" +
                    "    B.ROWID,\n" +
                    "    B.Bepunktung,\n" +
                    "    T.text\n" +
                    "FROM Bewertung as B, Projekt as P \n" +
                    "LEFT JOIN Optionaler_Text as T \n" +
                    "ON T.BewertungID = B.BewertungID \n" +
                    "WHERE B.ProjektID= P.ProjektID \n" +
                    "AND B.ProjektID = ? ;";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,projektid);
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("bewertungid", resultSet.getObject(1));
                entity.put("punktzahl", resultSet.getObject(2));
                entity.put("text", resultSet.getObject(3));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return entities;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }


    @Path("projekte/{projektid}/aufgaben")
    @GET // GET http://localhost:8080/projekte/{projektid}/aufgaben
    public List<Map<String, Object>> postAufgaben (@PathParam("projektid") String projektid ) throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement;

            String sql ="SELECT\n" +
                    "A.* , A.ROWID\n" +
                    "FROM Aufgabe as A, Projekt as P\n" +
                    "WHERE P.ProjektID= A.ProjektID\n" +
                    "AND P.ProjektID = ? ;";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,projektid);
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("aufgabeid", resultSet.getObject(6));
                entity.put("deadline", resultSet.getObject(3));
                entity.put("beschreibung", resultSet.getObject(2));
                entity.put("status", resultSet.getObject(5));
                entity.put("prioritaet", resultSet.getObject(4));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return entities;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }


    @Path("projekte/{projektid}/spezialisten")
    @GET // GET http://localhost:8080/projekte/{projektid}/spezialisten
    public List<Map<String, Object>> postSpezialisten (@PathParam("projektid") String projektid ) throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement;

            String sql ="SELECT\n" +
                    "S.ROWID,\n" +
                    "S.Verfugbarkeitsstatus,\n" +
                    "S.\"E-Mail-Adresse\",\n" +
                    "N.Passwort\n" +
                    "FROM  Arbeitet_An as A, Spezialist as S, Nutzer as N\n" +
                    "WHERE A.\"E-Mail-Adresse\" = S.\"E-Mail-Adresse\"\n" +
                    "AND  A.\"E-Mail-Adresse\" = N.\"E-Mail-Adresse\"\n" +
                    "AND A.ProjektID = ? ;";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,projektid);
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("spezialistid", resultSet.getObject(1));
                entity.put("verfuegbarkeitsstatus", resultSet.getObject(2));
                entity.put("email", resultSet.getObject(3));
                entity.put("passwort", resultSet.getObject(4));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return entities;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

//*************************************************************************************************************

    @Path("entwickler")
    @GET // GET http://localhost:8080/entwickler
    public List<Map<String, Object>> getEntwickler (
            @QueryParam("kuerzel") @DefaultValue("") String kuerzel) throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement;
        String sql = "SELECT Nutzer.ROWID, Spezialist.ROWID, Entwickler.ROWID, \n" +
                     "Spezialist.* ,Nutzer.Passwort, Kurzel \n" +
                     "FROM Entwickler, Spezialist, Nutzer \n" +
                     "WHERE Nutzer.\"E-Mail-Adresse\" = Spezialist.\"E-Mail-Adresse\" \n " +
                     "AND Entwickler.\"E-Mail-Adresse\" = Spezialist.\"E-Mail-Adresse\"";


        if (!kuerzel .isEmpty()) {
            sql = sql + "AND Kurzel = ? ;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,kuerzel );
        }
        else {
            sql = sql + ";";
            preparedStatement = connection.prepareStatement(sql);
        }

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("nutzerid", resultSet.getObject(1));
            entity.put("spezialistid", resultSet.getObject(2));
            entity.put("entwicklerid", resultSet.getObject(3));
            entity.put("email", resultSet.getObject(4));
            entity.put("password", resultSet.getObject(6));
            entity.put("verfuegbarkeitsstatus", resultSet.getObject(5));
            entity.put("kuerzel", resultSet.getObject(7));

            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities;
    }


    @Path("entwickler")
    @POST // GET http://localhost:8080/entwickler
    public Response postEntwickler(@FormDataParam("email") String email,
                                     @FormDataParam("password") String password,
                                     @FormDataParam("verfuegbarkeitsstatus") String verfuegbarkeitsstatus,
                                     @FormDataParam("kuerzel") String kuerzel,
                                     @FormDataParam("benennung") String benennung  ) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            addSpezialist(email,password,verfuegbarkeitsstatus,connection);

            if (getEntwickler(kuerzel).isEmpty()) {
                String sql = "INSERT INTO Entwickler VALUES( ? , ? );";
                PreparedStatement preparedStatement = connection
                        .prepareStatement(sql);
                preparedStatement.setObject(1, email);
                preparedStatement.setObject(2, kuerzel);
                preparedStatement.closeOnCompletion();
                preparedStatement.executeUpdate();
            }

            String programmierspracheId = addProgrammiersprache(benennung, connection);

            //String programmierspracheId = getProgrammiersprache(benennung)
              //      .get(0).get("id").toString();

            addBeherrscht(kuerzel, programmierspracheId,connection);

            connection.commit();
            return Response.status(Response.Status.fromStatusCode(201))
                    .header("location",uriInfo.getAbsolutePath()+"/"+getEntwickler(kuerzel).get(0).get("entwicklerid").toString())
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }
    }

    @Path("programmiersprache")
    @GET // GET http://localhost:8080/programmiersprache
    public List<Map<String, Object>> getProgrammiersprache(@QueryParam("benennung") @DefaultValue("") String benennung) throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement;
            String sql = "SELECT * FROM Programmiersprache ";

            if (!benennung.isEmpty()) {
                sql = sql + "WHERE Name = ? ;";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1,benennung);
            }
            else {
                sql = sql + ";";
                preparedStatement = connection.prepareStatement(sql);
            }

            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("id", resultSet.getObject(1));
                entity.put("benennung", resultSet.getObject(2));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return entities;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private void addBeherrscht(String kuerzel, String programmierspracheId,Connection connection) throws SQLException {
        String sql = "INSERT INTO Beherrscht VALUES( ? , ? ,? );";
        PreparedStatement preparedStatement = connection
                .prepareStatement(sql);
        preparedStatement.setObject(1, kuerzel);
        preparedStatement.setObject(2, programmierspracheId);
        preparedStatement.setObject(3, "3");
        preparedStatement.closeOnCompletion();
        preparedStatement.executeUpdate();
    }

    private String addProgrammiersprache(String benennung, Connection connection) throws SQLException {

        if ( getProgrammiersprache(benennung).isEmpty()) {
            int size = getProgrammiersprache("").size()+1;
            String sql = "INSERT INTO Programmiersprache (Name) VALUES ( ?); ";
            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setObject(1, benennung);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();
            return String.valueOf(size);
        }
        return getProgrammiersprache(benennung).get(0).get("id").toString();
    }


//*************************************************************************************************************

    @Path("programmierer")
    @GET // GET http://localhost:8080/bar => Bad Request; http://localhost/bar?foo=xyz => OK
    public Response getProgrammierer() {
        return Response.status(Response.Status.fromStatusCode(301))
                .header("location",uriInfo.getBaseUri().getPath()+"entwickler")
                .build();
    }

//*************************************************************************************************************

    @Path("projekte")
    @RolesAllowed({"KUNDE"})
    @POST // GET http://localhost:8080/projekte
    public Response postProjekte(@FormDataParam("name") @DefaultValue("") String name,
                                   @FormDataParam("deadline") @DefaultValue("") String deadline ) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            User user = (User) securityContext.getUserPrincipal();
            String telefon = getKunde(user.getName(), "").get(0).get("telefon").toString();
            var projektleiters = getProjektleiter("","");
            String projektleiter = projektleiters
                    .get((int)(Math.random()*projektleiters.size()))
                    .get("email").toString();

            String sql = "INSERT INTO Projekt " +
                         "(Projektname, Projektdeadline, Telefonnummer, \"E-Mail-Adresse\") \n" +
                         "VALUES( ? , ? , ? , ? ) ;";

            PreparedStatement preparedStatement = connection
                        .prepareStatement(sql);

            preparedStatement.setObject(1, name);
            preparedStatement.setObject(2, deadline);
            preparedStatement.setObject(3, telefon);
            preparedStatement.setObject(4, projektleiter);

            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();


            connection.commit();
            return Response.status(Response.Status.fromStatusCode(201))
                    .header("location",uriInfo.getAbsolutePath()+"/"+getProjekte().size()+1)
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }
    }


    @Path("projekte/{projektid}/bewertungen")
    @RolesAllowed({"KUNDE"})
    @POST // GET http://localhost:8080/projekte/{projektid}/bewertungen
    public Response postBewertungen(@PathParam("projektid") @DefaultValue("") String projektid,
                                    @FormDataParam("punktzahl") @DefaultValue("") String punktzahl,
                                    @FormDataParam("text") @DefaultValue("") String text) throws SQLException {
        Connection connection = dataSource.getConnection();

        try {
            connection.setAutoCommit(false);
            if (getProjekte()
                    .stream()
                    .filter(a->a.get("projektid").toString().equals(projektid))
                    .count()==0){
                connection.close();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message","Resource Not Found")).build();
            }

            User user = (User) securityContext.getUserPrincipal();
            String telefon = getKunde(user.getName(), "").get(0).get("telefon").toString();


            String sql = "INSERT INTO Bewertung " +
                    "(Bepunktung, Telefonnummer, ProjektID) \n" +
                    "VALUES( ? , ? , ? ) ;";

            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setObject(1, punktzahl);
            preparedStatement.setObject(2, telefon);
            preparedStatement.setObject(3, projektid);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();

            connection.commit();

            var bewertungen =  getBewertungen(projektid);
            String bewertungID = getBewertungen(projektid).get(bewertungen.size()-1).get("bewertungid").toString();

            if (!text.isEmpty()){
                sql = "INSERT INTO Optionaler_Text " +
                        "(Text, BewertungID) \n" +
                        "VALUES( ? , ? ) ;";
                preparedStatement = connection
                        .prepareStatement(sql);
                preparedStatement.setObject(1, text);
                preparedStatement.setObject(2, bewertungID);
                preparedStatement.closeOnCompletion();
                preparedStatement.executeUpdate();
                connection.commit();
            }


            return Response.status(Response.Status.fromStatusCode(204))
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }
    }

//*************************************************************************************************************


    private boolean BewertungNotExist(String bewertungid ) throws SQLException {
        Connection connection = dataSource.getConnection();
        String sql = "SELECT ROWID FROM  Bewertung " +
                     "WHERE ROWID = ? ;";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setObject(1,bewertungid);

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity = null;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("rowid", resultSet.getObject(1));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities.isEmpty();
    }

    private boolean textNotExist(String bewertungid ) throws SQLException {
        Connection connection = dataSource.getConnection();
        String sql = "SELECT ROWID FROM  Optionaler_Text " +
                "WHERE BewertungID = ? ;";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setObject(1,bewertungid);

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity = null;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("rowid", resultSet.getObject(1));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities.isEmpty();
    }

    @Path("projekte/bewertungen/{bewertungid}")
    @RolesAllowed({"KUNDE"})
    @PATCH // GET http://localhost:8080/projekte/bewertungen/{bewertungid}
    public Response pastBewertungen(@PathParam("bewertungid") @DefaultValue("") String bewertungid,
                                    @FormDataParam("punktzahl") @DefaultValue("") String punktzahl,
                                    @FormDataParam("text") @DefaultValue("") String text) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            if (BewertungNotExist(bewertungid)){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message","Resource Not Found")).build();
            }

            String sql = "UPDATE Bewertung \n" +
                         "SET  Bepunktung = ? \n" +
                         "WHERE ROWID = ? ;";

            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setObject(1, punktzahl);
            preparedStatement.setObject(2, bewertungid);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();
            connection.commit();

            if ( textNotExist(bewertungid) && !text.isEmpty()){
                sql =   "INSERT INTO Optionaler_Text (Text,BewertungID) \n" +
                        "VALUES ( ? , ?) ;" ;
                preparedStatement = connection
                        .prepareStatement(sql);
                preparedStatement.setObject(1, text);
                preparedStatement.setObject(2, bewertungid);
                preparedStatement.closeOnCompletion();
                preparedStatement.executeUpdate();
                connection.commit();
            }
            else if (!text.isEmpty()){
                sql = "UPDATE Optionaler_Text \n" +
                        "SET  Text = ? \n" +
                        "WHERE BewertungID = ? ;";
                preparedStatement = connection
                        .prepareStatement(sql);
                preparedStatement.setObject(1, text);
                preparedStatement.setObject(2, bewertungid);
                preparedStatement.closeOnCompletion();
                preparedStatement.executeUpdate();
                connection.commit();
            }

            return Response.status(Response.Status.fromStatusCode(204))
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }
    }


    @Path("projekte/bewertungen/{bewertungid}")
    @RolesAllowed({"KUNDE"})
    @DELETE // GET http://localhost:8080/projekte/bewertungen/{bewertungid}
    public Response deleteBewertungen(@PathParam("bewertungid") @DefaultValue("") String bewertungid
                                   ) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            if (BewertungNotExist(bewertungid)){
                connection.close();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message","Resource Not Found")).build();}

            String sql = "DELETE FROM Bewertung \n" +
                         "WHERE ROWID = ? ";

            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setObject(1, bewertungid);
            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();
            connection.commit();

            return Response.status(Response.Status.fromStatusCode(204))
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            connection.close();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message","Resource Not Found")).build();
        }
    }

//*************************************************************************************************************


    @Path("projekte/{projektid}/aufgaben")
    @RolesAllowed({"PROJEKTLEITER"})
    @POST // GET http://localhost:8080/projekte/{projektid}/aufgaben
    public Response postAufgaben(@PathParam("projektid") @DefaultValue("") String projektid,
                                 @FormDataParam("deadline") @DefaultValue("") String deadline,
                                 @FormDataParam("beschreibung") @DefaultValue("") String beschreibung,
                                 @FormDataParam("status") @DefaultValue("") String status,
                                 @FormDataParam("prioritaet") @DefaultValue("") String prioritaet) throws SQLException {

        Connection connection = dataSource.getConnection();

        try {
            connection.setAutoCommit(false);
            if (getProjekte()
                    .stream()
                    .filter(a->a.get("projektid").toString().equals(projektid))
                    .count()==0){
                connection.close();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message","Resource Not Found")).build();
            }


            String sql = "INSERT INTO Aufgabe " +
                    "(Beschreibung, Deadline, Vermerk, Status, ProjektID) \n" +
                    "VALUES( ? , ? , ? , ? , ?) ;";

            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setObject(1, beschreibung);
            preparedStatement.setObject(2, deadline);
            preparedStatement.setObject(3, prioritaet);
            preparedStatement.setObject(4, status);
            preparedStatement.setObject(5, projektid);

            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();

            connection.commit();

            return Response.status(Response.Status.fromStatusCode(204))
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }
    }



    @Path("projekte/{projektid}/spezialisten")
    @RolesAllowed({"PROJEKTLEITER"})
    @POST // GET http://localhost:8080/projekte/{projektid}/spezialisten
    public Response postSpezialisten(@PathParam("projektid") @DefaultValue("") String projektid,
                                    @FormDataParam("spezialistid") @DefaultValue("") String spezialistid
                                 ) throws SQLException {

        Connection connection = dataSource.getConnection();

        try {
            connection.setAutoCommit(false);
            if (getProjekte()
                    .stream()
                    .filter(a->a.get("projektid").toString().equals(projektid))
                    .count()==0){
                connection.close();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message","Resource Not Found")).build();
            }


            String spezialist = getSpezialistFromRowid(spezialistid);

            String sql = "INSERT INTO Arbeitet_An " +
                    "(\"E-Mail-Adresse\", projektid) \n" +
                    "VALUES( ? , ?) ;";

            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setObject(1, spezialist);
            preparedStatement.setObject(2, projektid);

            preparedStatement.closeOnCompletion();
            preparedStatement.executeUpdate();

            connection.commit();

            return Response.status(Response.Status.fromStatusCode(204))
                    .build();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage());
            connection.close();
            var message = new HashMap<String,String>();
            message.put("message",e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(message).build();
        }
    }

    private String getSpezialistFromRowid(String spezialistid) throws SQLException {
        Connection connection = dataSource.getConnection();
        String sql = "SELECT \"E-Mail-Adresse\" FROM  Spezialist " +
                "WHERE ROWID = ? ;";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setObject(1,spezialistid);

        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity = null;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("email", resultSet.getObject(1));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return entities.get(0).get("email").toString();
    }


}