package GetData;

public class WrongAuthDataException extends Exception{
    @Override
    public String toString() {
        return "Incorrect login/password";
    }
}
