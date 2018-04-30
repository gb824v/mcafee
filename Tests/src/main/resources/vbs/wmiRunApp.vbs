strComputer =  
strDomain =  
strUser = 
strPassword = 
strCmd =

Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")

If IsEmpty (strDomain) Then
Set objSWbemServices = objSWbemLocator.ConnectServer(strComputer,"root\cimv2",strUser,strPassword,"MS_409","ntlmdomain:" + strDomain) 
Else
Set objSWbemServices = objSWbemLocator.ConnectServer(strComputer,"root\cimv2",strUser,strPassword)
End If
Set Process = objSWbemServices.Get("Win32_Process")
	
	result = Process.Create(strCmd, , , ProcessId)
	
    If (result <> 0) Then
    	WScript.Echo "Creating Remote Process Failed: " & result
    	Wscript.Quit
    End If

    Wscript.Echo "process id is: " & ProcessId