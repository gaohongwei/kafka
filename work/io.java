// BufferRead
BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
while ((line = bufferedReader.readLine()) != null)
{
    records.add(line);
}
// close the BufferedReader when we're done
bufferedReader.close();

int portNumber = Integer.parseInt(args[0]);
try ( 
    ServerSocket serverSocket = new ServerSocket(portNumber);
    Socket clientSocket = serverSocket.accept();
    PrintWriter out =
        new PrintWriter(clientSocket.getOutputStream(), true);
    BufferedReader in = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
) {
