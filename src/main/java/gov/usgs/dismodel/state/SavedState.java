package gov.usgs.dismodel.state;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.beanutils.BeanUtils;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType(propOrder = {"simModel", "displaySettings"  })
public class SavedState {
	@XmlElement(name = "simulationState")
	private SimulationDataModel simModel;
	@XmlElement
	private DisplayStateStore displaySettings;

	public SavedState() {
		super();
	}
	
	public SavedState(SimulationDataModel simModel,
			DisplayStateStore displaySettings) {
		super();
		this.simModel = simModel;
		this.displaySettings = displaySettings;
	}



	public SimulationDataModel getSimModel() {
		return simModel;
	}

	public void setSimModel(SimulationDataModel simModel) {
		this.simModel = simModel;
	}

	public DisplayStateStore getDisplaySettings() {
		return displaySettings;
	}

	public void setDisplaySettings(DisplayStateStore displaySettings) {
		this.displaySettings = displaySettings;
	}
	
	
    public static void writeToXML(SavedState state, File xmlFile) throws JAXBException, IOException {
        BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(xmlFile));
        JAXBContext context = JAXBContext.newInstance(SavedState.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(state, outStream);
        outStream.close();
    }

    public static SavedState readXML(File xmlFile) throws IOException, JAXBException {
        BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(xmlFile));
        JAXBContext context = JAXBContext.newInstance(SavedState.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        // note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        // unmarshaller.setProperty(Unmarshaller., value)
        SavedState readState = (SavedState) (unmarshaller.unmarshal(inStream));
        inStream.close();
        return readState;
    }

	
	
	
}
