package integration.messaging.hl7.datamodel;

import org.apache.commons.lang3.StringUtils;

/**
 * A message component.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageComponent {

    /**
     * Gets the components value.
     * 
     * @return
     * @throws Exception
     */
    public abstract String value() throws Exception;

    /**
     * Sets the components value.
     * 
     * @param value
     * @throws Exception
     */
    public abstract void setValue(String value) throws Exception;

    /**
     * Clears the components value.
     * 
     * @throws Exception
     */
    public abstract void clear() throws Exception;

    /**
     * Copies the value from the sourceComponent to this component
     * 
     * @param sourceComponent
     */
    public void copy(MessageComponent sourceComponent) throws Exception {
        this.setValue(sourceComponent.value());
    }

    /**
     * Copies the source component value to this component if this component is
     * empty.
     * 
     * @param sourceComponent
     * @throws Exception
     */
    public void copyIfEmpty(MessageComponent sourceComponent) throws Exception {
        if (StringUtils.isBlank(this.value())) {
            copy(sourceComponent);
        }
    }

    /**
     * Moves the value from the sourceComponent to this component. This is a copy
     * followed by a clear.
     * 
     * @param sourceComponent
     */
    public void move(MessageComponent sourceComponent) throws Exception {
        copy(sourceComponent);
        sourceComponent.clear();
    }

    /**
     * Swaps the values of the 2 components.
     * 
     * @param sourceComponent
     * @throws Exception
     */
    public void swap(MessageComponent sourceComponent) throws Exception {
        String thisValue = this.value();
        String sourceComponentValue = sourceComponent.value();

        this.setValue(sourceComponentValue);
        sourceComponent.setValue(thisValue);
    }

    /**
     * Appends a value to the end of the components value.
     * 
     * @param valueToAppend
     * @throws Exception
     */
    public void append(String valueToAppend) throws Exception {
        String currentValue = this.value();

        this.setValue(currentValue + valueToAppend);
    }

    /**
     * Prepends a value to the beginning of the components value.
     * 
     * @param valueToPrepend
     * @throws Exception
     */
    public void prepend(String valueToPrepend) throws Exception {
        String currentValue = this.value();

        this.setValue(valueToPrepend + currentValue);
    }

    /**
     * Returns this components value unescaped.
     * 
     * @return
     * @throws Exception
     */
    public String unescapedValue() throws Exception {
        return EscapeSequenceEnum.unescape(this.value());
    }

    /**
     * Returns this components value escaped.
     * 
     * @return
     * @throws Exception
     */
    public String escapedValue() throws Exception {
        return EscapeSequenceEnum.escape(this.value());
    }
}