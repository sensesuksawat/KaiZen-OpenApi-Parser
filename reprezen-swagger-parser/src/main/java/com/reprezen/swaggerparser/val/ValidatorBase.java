package com.reprezen.swaggerparser.val;

import static com.reprezen.swaggerparser.val.Messages.m;
import static com.reprezen.swaggerparser.val.NumericUtils.le;
import static com.reprezen.swaggerparser.val.NumericUtils.lt;
import static com.reprezen.swaggerparser.val.NumericUtils.zero;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.print.attribute.standard.Severity;

import com.google.common.collect.Sets;
import com.reprezen.swaggerparser.jsonoverlay.JsonOverlay;

public abstract class ValidatorBase<T> implements Validator<T> {

    @Override
    public ValidationResults validate(T object) {
        ValidationResults results = new ValidationResults();
        validate(object, results);
        return results;
    }

    @Override
    public void validate(final T object, final ValidationResults results, String crumb) {
        results.withCrumb(crumb, new Runnable() {
            @Override
            public void run() {
                validate(object, results);
            }
        });
    }

    public void validateString(final String value, final ValidationResults results, final boolean required,
            final String pattern, final String crumb) {
        checkMissing(required, value, results, crumb);
        if (value != null && !value.matches(pattern)) {
            results.addError(m.msg("PatternMatchFail|String value does not match required pattern", value, pattern),
                    crumb);
        }
    }

    public void validateUrl(String value, ValidationResults results, boolean required, String crumb) {
        validateUrl(value, results, required, crumb, false);
    }

    public void validateUrl(String value, ValidationResults results, boolean required, String crumb,
            boolean allowVars) {
        validateUrl(value, results, required, crumb, allowVars, Severity.ERROR);
    }

    public void validateUrl(final String value, final ValidationResults results, boolean required, String crumb,
            final boolean allowVars, final Severity severity) {
        validateString(value, results, required, null, crumb);
        checkUrl(value, results, allowVars, severity, crumb);
    }

    public void validateEmail(final String value, final ValidationResults results, boolean required, String crumb) {
        validateString(value, results, required, null, crumb);
        checkEmail(value, results, crumb);
    }

    public <N extends Number> void validatePositive(final N value, final ValidationResults results,
            final boolean required, final String crumb) {
        checkMissing(required, value, results, crumb);
        if (value != null && le(value, zero(value))) {
            results.addError(m.msg("ReqPositive|Value must be strictly positive", crumb, value), crumb);
        }
    }

    public <N extends Number> void validateNonNegative(final N value, final ValidationResults results,
            final boolean required, final String crumb) {
        checkMissing(required, value, results, crumb);
        if (value != null && lt(value, zero(value))) {
            results.addError(m.msg("ReqPositive|Value must be strictly positive", crumb, value), crumb);
        }
    }

    public void validateNonEmpty(Collection<?> values, boolean hasValues, ValidationResults results, String crumb) {
        if (hasValues && values.isEmpty()) {
            results.addError(m.msg("EmptyColl|Collection may not be empty"), crumb);
        }
    }

    public <V> void validateUnique(final Collection<V> values, final ValidationResults results, final String crumb) {
        results.withCrumb(crumb, new Runnable() {
            Set<V> seen = Sets.newHashSet();

            @Override
            public void run() {
                int i = 0;
                for (V value : values) {
                    if (seen.contains(value)) {
                        results.addError(m.msg("DuplicateValue|Value appeared already", value, crumb), "[" + i + "]");
                    } else {
                        seen.add(value);
                    }
                    i += 1;
                }
            }
        });
    }

    public void validatePattern(final String pattern, final ValidationResults results, final boolean required,
            final String crumb) {
        checkMissing(required, pattern, results, crumb);
        if (pattern != null) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                results.addWarning(
                        m.msg("BadPattern|Pattern is not a valid Java Regular Expression but may be valid ECMA 262",
                                pattern),
                        crumb);
            }
        }
    }

    public <F> void validateField(final F value, final ValidationResults results, final boolean required,
            final String crumb, final Validator<F> validator) {
        checkMissing(required, value, results, crumb);
        validator.validate(value, results, crumb);
    }

    public <V> void validateList(final Collection<? extends V> value, final boolean isPresent,
            final ValidationResults results, final boolean required, final String crumb, final Validator<V> validator) {
        if (required && !isPresent) {
            results.addError(m.msg("MissingField|Required field is missing", crumb), crumb);
        }
        if (isPresent && validator != null) {
            int i = 0;
            for (V element : value) {
                validator.validate(element, results, "[" + i++ + "]");
            }
        }
    }

    public <V> void validateMap(final Map<String, ? extends V> value, final ValidationResults results,
            final boolean required, final String crumb, final Pattern pattern, final Validator<V> validator) {
        checkMissing(required, value, results, crumb);
        results.withCrumb(crumb, new Runnable() {

            @Override
            public void run() {
                for (Entry<String, ? extends V> entry : value.entrySet()) {
                    checkKey(entry.getKey(), pattern, results);
                    if (validator != null) {
                        validator.validate(entry.getValue(), results, entry.getKey());
                    }
                }
            }
        });
    }

    public void validateExtensions(final Map<String, Object> extensions, final ValidationResults results) {
        validateExtensions(extensions, results, null);
    }

    public void validateExtensions(final Map<String, Object> extensions, final ValidationResults results,
            String crumb) {
        validateMap(extensions, results, false, crumb, null, null);
    }

    private void checkMissing(boolean required, final Object value, final ValidationResults results,
            final String crumb) {
        if (required && isMissing(value)) {
            results.addError(m.msg("MissingField|Required field is missing", crumb), crumb);
        }
    }

    private void checkKey(final String key, final Pattern pattern, final ValidationResults results) {
        if (!pattern.matcher(key).matches()) {
            results.addError(m.msg("BadKey|Invalid key in map", key, pattern), key);
        }
    }

    private void checkUrl(String url, ValidationResults results, boolean allowVars, Severity severity, String crumb) {
        // TODO implement this
    }

    private void checkEmail(String email, ValidationResults results, String crumb) {
        // TODO implement this
    }

    private boolean isMissing(Object value) {
        return value instanceof JsonOverlay ? ((JsonOverlay<?>) value).isMissing() : value == null;
    }
}
