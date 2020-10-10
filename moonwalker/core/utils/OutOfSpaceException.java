/*
    Copyright (C) 2020 Micha³ Kullass

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package moonwalker.core.utils;

public class OutOfSpaceException extends Exception
{
	public OutOfSpaceException()
	{
		super();
	}
	public OutOfSpaceException(String message)
	{
		super(message);
	}
	public OutOfSpaceException(Throwable cause)
	{
		super(cause);
	}
	public OutOfSpaceException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public OutOfSpaceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
