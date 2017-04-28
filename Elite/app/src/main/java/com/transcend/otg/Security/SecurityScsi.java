package com.transcend.otg.Security;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.transcend.otg.Constant.Constant;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by RD13_win10 on 2017/4/18.
 */

public class SecurityScsi {
    private static UsbDevice usbDevice;
    private UsbDeviceConnection usbDeviceConnection ;
    private UsbEndpoint usbEndpointIn , usbEndpointOut;
    private UsbInterface usbInterface;
    private static UsbManager usbManager;

    private static final boolean FORCE_CLAIM = true;
    private static final int MAX_BUFFER_SIZE = 512;
    public static final int CBW_SIGNATURE = 1128420181;
    public static final int CBS_SIGNATURE = 1396855637;
    public static final int CBW_SIZE = 31;
    public static final int CBS_SIZE = 13;
    public static final int SCSI_IOCTL_DATA_OUT = 0x80 ;
    public static final int SCSI_IOCTL_DATA_IN = 0x00 ;

    private boolean isSecurityLock = false;
    private boolean isSecurityEnable = true;

    private static SecurityScsi instance ;
    private int SecurityStatus = Constant.SECURITY_DEVICE_EMPTY;
    private int previousPage = 0;

    public static synchronized SecurityScsi getInstance(UsbDevice Device , UsbManager Manager , boolean reCreate){
        if( instance == null || reCreate){
            instance = new SecurityScsi(Device , Manager);
        }
        return instance ;
    }

    private SecurityScsi(UsbDevice Device , UsbManager Manager)
    {
        usbDevice = Device ;
        usbManager = Manager ;
        setupUsbEndpoint();
    }

    private void setupUsbEndpoint()
    {
        UsbEndpoint EndIn = null, EndOut = null;
        if(usbDevice.getInterfaceCount() > 0){
            usbInterface = usbDevice.getInterface(0);
            for( int i = 0 ; i < usbInterface.getEndpointCount() ; ++i){
                if( usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
                    if(usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN){
                        EndIn = usbInterface.getEndpoint(i);
                    }
                    else{
                        EndOut = usbInterface.getEndpoint(i);
                    }
                }
            }
        }

        if(EndIn != null && EndOut != null){
            usbEndpointIn = EndIn;
            usbEndpointOut = EndOut;
        }
    }

    public void SecurityLockActivity(String password)
    {
        try{
            ScsiLockCommand();
            setUserPassword(password);
            closeScsiTransfer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void SecurityUnlockActivity(String password){
        try {
            ScsiUnlockCommand();
            setUserPassword(password);
            closeScsiTransfer();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void SecurityDisableLockActivity(String password){
        try {
            ScsiUnlockCommand();
            setUserPassword(password);
            closeScsiTransfer();

            Thread.sleep(1000);
            ScsiDisableCommand();
            setUserPassword(password);
            closeScsiTransfer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getSecurityStatus()
    {
        return SecurityStatus ;
    }

    public void setSecurityStatus( int status ){
        SecurityStatus = status ;
    }

    public void setPreviousPage(int page){
        previousPage = page ;
    }

    public int getPreviousPage(){
        return previousPage;
    }


    public int checkSecurityStatus(){
        ScsiIDCommand();
        ByteBuffer byteReceive = ByteBuffer.allocate(512);
        if( ReceiveCommand(byteReceive) ){
            parsingScsiIDInformation(byteReceive);
            try{Thread.sleep(1500);}
            catch (Exception e){e.printStackTrace();}

            if(isSecurityEnable){
                if(isSecurityLock)
                    SecurityStatus = Constant.SECURITY_LOCK;
                else
                    SecurityStatus = Constant.SECURITY_UNLOCK;
            }
            else{
                SecurityStatus = Constant.SECURITY_DISABLE;
            }
        }else{
            SecurityStatus = Constant.SECURITY_DEVICE_EMPTY;
        }
        closeScsiTransfer();

        return SecurityStatus;
    }

    private void ScsiLockCommand()
    {
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        usbDeviceConnection.claimInterface(usbInterface , FORCE_CLAIM);
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);

         /* Vendor Lock */
        byteBuffer.put((byte)0x85);
        byteBuffer.put((byte)0x0A);
        byteBuffer.put((byte)0x06);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xD6);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x01);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xF1);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x4F);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xC2);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xB0);
        byteBuffer.put((byte)0x00);

        SendCommand(byteBuffer,true);
    }

    private void ScsiUnlockCommand(){
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        usbDeviceConnection.claimInterface(usbInterface , FORCE_CLAIM);
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);

        /* Vendor Unlock */
        byteBuffer.put((byte)0x85);
        byteBuffer.put((byte)0x0A);
        byteBuffer.put((byte)0x06);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xD6);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x01);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xF2);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x4F);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xC2);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xB0);
        byteBuffer.put((byte)0x00);

        SendCommand(byteBuffer , true) ;
    }

    private void ScsiDisableCommand(){
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        usbDeviceConnection.claimInterface(usbInterface , FORCE_CLAIM);
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);

        /* Vendor Disable */
        byteBuffer.put((byte)0x85);
        byteBuffer.put((byte)0x0A);
        byteBuffer.put((byte)0x06);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xD6);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x01);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xF6);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x4F);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xC2);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xB0);
        byteBuffer.put((byte)0x00);

        SendCommand(byteBuffer , true) ;
    }

    private void ScsiIDCommand()
    {
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        usbDeviceConnection.claimInterface(usbInterface , FORCE_CLAIM);
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        /* ID */
        byteBuffer.put((byte)0xA1);
        byteBuffer.put((byte)0x08);
        byteBuffer.put((byte)0x0E);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x01);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0xA0);
        byteBuffer.put((byte)0xEC);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);
        byteBuffer.put((byte)0x00);

        SendCommand(byteBuffer , false) ;
    }

    private Boolean SendCommand(ByteBuffer bufCommand , Boolean isWrite)
    {
        ByteBuffer cbw = ByteBuffer.allocate(CBW_SIZE);
        cbw.order(ByteOrder.LITTLE_ENDIAN);
        cbw.putInt(CBW_SIGNATURE);
        cbw.putInt(10);
        cbw.putInt(MAX_BUFFER_SIZE);

        if( isWrite == true )
            cbw.put((byte)SCSI_IOCTL_DATA_IN);
        else
            cbw.put((byte)SCSI_IOCTL_DATA_OUT);

        cbw.put((byte)0);
        cbw.put((byte)bufCommand.capacity());
        cbw.put(bufCommand.array());
        int ret = usbDeviceConnection.bulkTransfer(usbEndpointOut, cbw.array(), cbw.capacity() ,3000);   /// do in other thread
        if(ret < 0 )
            return false;

        return true;
    }

    private Boolean ReceiveCommand( ByteBuffer byteReceive){
        byteReceive.order(ByteOrder.LITTLE_ENDIAN);
        boolean get_info = false;
        while(!get_info) {
            //int ret = usbDeviceConnection.bulkTransfer(usbEndpointIn, byteReceive.array(), byteReceive.capacity(), 500);
            if(usbDeviceConnection.bulkTransfer(usbEndpointIn, byteReceive.array(), byteReceive.capacity(), 2000) > 0)
                return true ;
            try {
                Thread.sleep(500);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }

    private void setUserPassword(String password){

        byte[] bytesPassword = password.getBytes();
        ByteBuffer Password = ByteBuffer.allocate(512);
        Password.order(ByteOrder.LITTLE_ENDIAN);

        Password.put((byte)0x00);
        Password.put((byte)0x01);
        for(int i = 0 ; i < bytesPassword.length ; ++i )
        {
            Password.put(bytesPassword[i]);
        }

        int ret = usbDeviceConnection.bulkTransfer(usbEndpointOut , Password.array() , Password.capacity() , 3000);
    }

    private void closeScsiTransfer()
    {
        usbDeviceConnection.releaseInterface(usbInterface);
        usbDeviceConnection.close();
    }

    private void parsingScsiIDInformation(ByteBuffer IDTableArray)
    {
        //String IDinformation = "" ;

        /* Security enable*/
        String isSecurity = "";
        if ( (IDTableArray.get(0xAA) & 0x02 ) == 0x02 ) {
            isSecurity = "1";
            isSecurityEnable = true;
        }
        else {
            isSecurity = "0";
            isSecurityEnable = false;
        }
        //IDinformation += isSecurity + "@";

        String SecurityUnlock = "";
        if ( (IDTableArray.get(0x100) & 0x04 ) == 0x04 ) {
            SecurityUnlock = "1";
            isSecurityLock = true;
        }
        else {
            SecurityUnlock = "0";
            isSecurityLock = false;
        }
        //IDinformation += SecurityUnlock ;
    }
}
