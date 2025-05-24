# command-line e-pood ehk CLEP

## Käivitamise juhend

Esmalt tuleb käivitada **Main** klass, mis töötab serverina.

Seejärel käivita **Client** ja järgi terminali juhiseid.

## Funktsionaalsus

- Server hoiab laoseisu ja tellimusi. ✅
- Sisselogitud töötajad saavad laoseisu muuta ja tellimusi vaadata. ✅
- Külastajad saavad laoseisu vaadata ja kaupa tellida. ✅
- Külastaja saab oma ostude ajalugu vaadata. ✅
- Tellimuse tegemisel saadetakse e-mail töötajale ja tellijale. ✅
- Tooteid saab nime ja kirjelduse järgi otsida. ✅
- Pood kogub toodete vaatamise kohta statistikat ja näitab, mis tooteid kõige rohkem vaadatakse / tellitakse. ✅

## Framework

**JavaMail API**

**SQLite**:
- Laoseis ja tooted
- Kasutaja sisselogimisandmed (kasutajanimed, paroolide hashid)
- Tellimused

## Multithreaded server

Server listens for client connections and starts a new thread `ClientConnection` for each one.

```java
try (ServerSocket ss = new ServerSocket(8080)) {
    while (true) {
        new Thread(new ClientConnection(ss.accept(), initiateConnection(DB_PATH))).start();
    }
}
```

## Client: Read/Write loop

Core idea of `Client` is to read message from server, get input from user and provide it to server -> get response and repeat whole thing over and over.

```java
while (true) {
    String serverMessage = io.read();
    System.out.println(serverMessage);

    if (serverMessage.equalsIgnoreCase("Logging off...") || serverMessage.equalsIgnoreCase("EXIT"))
        break;

    String userReply = userInput.readLine();
    io.write(userReply);
}
```

## User roles and session loop

After `Client` connects, the `Server` authenticates or registers the user and creates a role-specific User object (`Customer`, `Employee` or `Admin`).

```java
public void run() {
    User user = getUserOrExit();
    if (user == null) return;

    try (io; dbConnection) {
        runUserSession(user);
    }
}
```

Once login is successful, the user's session starts. Since database is using SQLite, database locking is highly possible — that is why `SQLException (database locked)` is handled specifically.

```java
private void runUserSession(User user) throws IOException {
    int triesLeft = 5;

    while (true) {
        try {
            user.handleSession();
            break;
        } catch (SQLException e) {
            if (shouldRetry(e, triesLeft)) {
                triesLeft--;
                sleepBriefly();
                continue;
            }
            io.write("Could not access database. Try again later.");
            break;
        }
    }
}
```

## User and role-based commands

Based on role of their `User` object they are given different sets of commands that are handled in `handleSession()`. Command tied to logging out returns `false` which terminates the session.

```java
public void handleSession() throws IOException, SQLException {
    while (true) {
        io.write(getMenu());
        String command = io.read();
        if (!handleCommand(command)) break; // logout
    }
}
```

## Customer - one of the Users
Each `Users` is linked to their unique ID from database and provided dedicated `IOUnit`.

```java
  public Customer(int userID, Queries queries, Helpers helpers, IOUnit io) throws SQLException, AddressException {
      super(userID, queries, helpers, io);
  }

  @Override
  protected String getMenu() {
      return "\n=== Customer Menu. Type in the number of command ===" +
              "\n1 - LOOK_UP" +
              "\n2 - PLACE ORDER" +
              "\n3 - VIEW MY ORDERS" +
              "\n4 - LOGOUT" +
              "\nEnter choice:";
  }


  @Override
  boolean handleCommand(String command) throws IOException, SQLException {
      switch (command) {
          case "1" -> {
              handleLookUp();
          }
          case "2" -> {
              placeOrder();
          }
          case "3" -> {
              getOrderHistory();
          }
          case "4" -> {
              io.write("Logging off...");
              return false;
          }
          default -> {
              io.write("Invalid command. Press any key to proceed to menu");
              io.read();
          }
      }
      return true;
  }
```

## Communication with DB

Communication with the database is handled via many-many methods in the `Queries` class. Class on creationg accepts a DB connection and parametrizes each query to **prevent SQL injections**.

```java

  public Queries(Connection connection) {
      this.connection = connection;
  }

  public static void prepareParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            switch (params[i]) {
                case Integer integer -> pstmt.setInt(i + 1, integer);
                case String s -> pstmt.setString(i + 1, s);
                case BigDecimal bg -> pstmt.setBigDecimal(i + 1, bg);
                case byte[] bytes -> pstmt.setBytes(i + 1, bytes);
                default -> throw new SQLException("Unsupported parameter type: " + params[i].getClass());
            }
        }
    }

  public boolean executeUpdate(String query, Object... params) throws SQLException {
      PreparedStatement pstms = connection.prepareStatement(query);
      prepareParams(pstms, params);
      return pstms.executeUpdate() > 0;
  }

  public boolean insertSaltAndHash(int id, byte[] hash, byte[] salt) throws SQLException {
      String query = "INSERT INTO user_credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";
      return executeUpdate(query, id, hash, salt);
  }
```

## Helpers 

The `Helpers` class does a lot (probably **too much**). It’s used for:

- Prompt validation
- Business logic
- Invoking queries
- Data transformation

## IOUnit
`IOUnit` is practically a wrapper around `DataInputStream` and `DataOutputStream`.  
It handles:

- Basic I/O
- Avoiding null/empty writes
- Implements `AutoCloseable` for clean shutdowns

```java
  public IOUnit(Socket socket) throws IOException {
      this.input = new DataInputStream(socket.getInputStream());
      this.output = new DataOutputStream(socket.getOutputStream());
  }

  public String read() throws IOException {
      return input.readUTF();
  }

  public void write(String message) throws IOException {
      if (message == null || message.isBlank()) { message = "";}
      output.writeUTF(message);
  }

  @Override
  public void close() throws Exception {
      input.close();
      output.close();
  }
```

## Database
![diagram-export-5-24-2025-8_06_47-PM](https://github.com/user-attachments/assets/68231238-a1b1-4b28-82fa-02abd08b6c6e)

### Rühmaliikmed
Georg Järvis

Daniil Krivko
