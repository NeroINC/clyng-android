package com.clyng.mobile;

/**
 * Created with IntelliJ IDEA.
 * User: ---
 * Date: 11/5/12
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public UserAlreadyExistsException(String message) {
        super( message );
    }
}
