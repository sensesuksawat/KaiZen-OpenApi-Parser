package com.reprezen.swaggerparser.val3;

import com.google.inject.Inject;
import com.reprezen.swaggerparser.model3.ExternalDocs;
import com.reprezen.swaggerparser.model3.Tag;
import com.reprezen.swaggerparser.val.ObjectValidatorBase;
import com.reprezen.swaggerparser.val.ValidationResults;
import com.reprezen.swaggerparser.val.Validator;

public class TagValidator extends ObjectValidatorBase<Tag> {

    @Inject
    Validator<ExternalDocs> externalDocsValidator;

    @Override
    public void validateModel(Tag tag, ValidationResults results) {
        validateString(tag.getName(), results, true, null, "name");
        validateField(tag.getExternalDocs(), results, false, "externalDocs", externalDocsValidator);
        validateExtensions(tag.getExtensions(), results);
    }

}
