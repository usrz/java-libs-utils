/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.utils.beans;

public interface PrimitivesBean {

    public boolean getBooleanValue();
    public byte getByteValue();
    public char getCharValue();
    public short getShortValue();
    public int getIntValue();
    public long getLongValue();
    public float getFloatValue();
    public double getDoubleValue();

    public void setBooleanValue(boolean booleanValue);
    public void setByteValue(byte byteValue);
    public void setCharValue(char charValue);
    public void setShortValue(short shortValue);
    public void setIntValue(int intValue);
    public void setLongValue(long longValue);
    public void setFloatValue(float floatValue);
    public void setDoubleValue(double doubleValue);

}
