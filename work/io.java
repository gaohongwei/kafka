// BufferRead
BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
while ((line = bufferedReader.readLine()) != null)
{
    records.add(line);
}
// close the BufferedReader when we're done
bufferedReader.close();
