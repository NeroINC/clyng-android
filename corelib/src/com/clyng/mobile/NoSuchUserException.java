package com.clyng.mobile;

/**
 * Created with IntelliJ IDEA.
 * User: ---
 * Date: 11/5/12
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoSuchUserException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoSuchUserException( String message )
    {
        super(message);
    }
}
