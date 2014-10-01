//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.28 at 12:26:20 PM CEST 
//


package de.uos.inf.ischedule.model.inrc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for DayOnRequests complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DayOnRequests">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DayOn" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="EmployeeID" type="{}ID"/>
 *                   &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="weight" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DayOnRequests", propOrder = {
    "dayOn"
})
public class InrcDayOnRequests {

    @XmlElement(name = "DayOn")
    protected List<InrcDayOnRequests.InrcDayOn> dayOn;

    /**
     * Gets the value of the dayOn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dayOn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDayOn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InrcDayOnRequests.InrcDayOn }
     * 
     * 
     */
    public List<InrcDayOnRequests.InrcDayOn> getDayOn() {
        if (dayOn == null) {
            dayOn = new ArrayList<InrcDayOnRequests.InrcDayOn>();
        }
        return this.dayOn;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="EmployeeID" type="{}ID"/>
     *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *       &lt;/sequence>
     *       &lt;attribute name="weight" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "employeeID",
        "date"
    })
    public static class InrcDayOn {

        @XmlElement(name = "EmployeeID", required = true)
        protected String employeeID;
        @XmlElement(name = "Date", required = true)
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar date;
        @XmlAttribute(name = "weight", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger weight;

        /**
         * Gets the value of the employeeID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEmployeeID() {
            return employeeID;
        }

        /**
         * Sets the value of the employeeID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEmployeeID(String value) {
            this.employeeID = value;
        }

        /**
         * Gets the value of the date property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getDate() {
            return date;
        }

        /**
         * Sets the value of the date property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setDate(XMLGregorianCalendar value) {
            this.date = value;
        }

        /**
         * Gets the value of the weight property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getWeight() {
            return weight;
        }

        /**
         * Sets the value of the weight property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setWeight(BigInteger value) {
            this.weight = value;
        }

    }

}
