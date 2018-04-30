strComputer =  
strDomain =  
strUser = 
strPassword = 

Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")

If IsEmpty (strDomain) Then
Set objSWbemServices = objSWbemLocator.ConnectServer(strComputer,"root\cimv2",strUser,strPassword,"MS_409","ntlmdomain:" + strDomain) 
Else
Set objSWbemServices = objSWbemLocator.ConnectServer(strComputer,"root\cimv2",strUser,strPassword)
End If

Set colItems = objSWbemServices.ExecQuery( _
    "SELECT * FROM Win32_Service",,48) 
For Each objItem in colItems 
    Wscript.Echo "Service Name: " & objItem.Name & VBNewLine _
        & "State: " & objItem.State
Next