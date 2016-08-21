package is.web.beans.denomination;

import is.ejb.bl.denominationModels.DenominationModelExportManager;
import is.ejb.dl.dao.DAODenominationModel;
import is.ejb.dl.entities.DenominationModelEntity;
import is.ejb.dl.entities.RealmEntity;
import is.web.beans.users.LoginBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean(name="denominationExportBean")
@SessionScoped
public class DenominationExportBean {
	
	@Inject
	private Logger logger;
	
	@Inject
	private DAODenominationModel daoDenominationModel;
	
	private final String ENCODING = DenominationModelExportManager.ENCODING;
	

	public DenominationExportBean() {

	}
	
	public StreamedContent getFile(DenominationModelEntity denominationModel) {
		String content = prepareFile(denominationModel);
		String fileName = generateFileName();
		InputStream stream = null;

		try {
			stream = new ByteArrayInputStream(content.trim().getBytes(ENCODING));
		} catch (UnsupportedEncodingException e) {
			logger.info("Unsupported encoding: " + ENCODING);
			stream = new ByteArrayInputStream(content.getBytes());
		}
		
		String contentType = "text/txt";
		return new DefaultStreamedContent(stream, contentType, fileName);
	}
	
	public void importModel(FileUploadEvent event){
		UploadedFile uploadedFile = event.getFile();
		DenominationModelExportManager exportManager = new DenominationModelExportManager();
		
		DenominationModelEntity modelEntity = null;
		String content = "";
		
		try {
			content = getContent(uploadedFile);
			modelEntity = exportManager.importModel(content, getRealm());
			
			if(modelEntity != null){
				daoDenominationModel.createOrUpdate(modelEntity);
				showGrowl(FacesMessage.SEVERITY_INFO, "Import", "Import successfully");
			}
			
		} catch (IOException e) {
			logger.info(e.getMessage());
		} catch (Exception e) {
			logger.info(e.getMessage());
			showGrowl(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
		}
	}

	private String prepareFile(DenominationModelEntity denominationModel) {
		DenominationModelExportManager exportManager = new DenominationModelExportManager();
		String fileContent = exportManager.exportModel(denominationModel);
		
		return fileContent;
	}
	
	private String generateFileName(){
		String name = "denomination_";
		Calendar calendar = Calendar.getInstance();
		long time = calendar.getTimeInMillis();
		name += String.valueOf(time) + ".txt";
		return name;
	}
	
	private String getContent(UploadedFile uploadedFile) throws IOException{
		String content = "";

		InputStream importFileStream = uploadedFile.getInputstream();
		byte[] contentByteArray = IOUtils.toByteArray(importFileStream); 
		if(importFileStream != null){
			importFileStream.close();
		}
		
		if(isUTF8BOM(contentByteArray)){
			int startIndex=3;
			content = new String(contentByteArray, startIndex, contentByteArray.length-startIndex, ENCODING);
			
		} else {
			content = new String(contentByteArray, ENCODING);
		}
		
		return content;
	}
	
	private boolean isUTF8BOM(byte[] inputBytes){
		final byte[] BOM_BYTES = {(byte) 239, (byte) 187, (byte) 191};
		
		byte[] testBytes = new byte[3];
		for(int i=0; i<testBytes.length; i++){
			testBytes[i] = inputBytes[i];
		}
		
		if(Arrays.equals(BOM_BYTES, testBytes)){
			return true;
		} else {
			return false;
		}
		
	}

	private RealmEntity getRealm(){
		FacesContext fc = FacesContext.getCurrentInstance();
		LoginBean loginBean = (LoginBean) fc.getApplication().evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
		RealmEntity realm = loginBean.getUser().getRealm();
		
		return realm;
	}
	
	private void showGrowl(Severity severity, String summary, String detail){
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
		RequestContext.getCurrentInstance().update("tabView:idDenominationModelGrowl");
	}
	
}
