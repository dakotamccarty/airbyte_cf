/* tslint:disable */
/* eslint-disable */
/**
 * Airbyte Configuration API
 * Airbyte Configuration API [https://airbyte.io](https://airbyte.io).  This API is a collection of HTTP RPC-style methods. While it is not a REST API, those familiar with REST should find the conventions of this API recognizable.  Here are some conventions that this API follows: * All endpoints are http POST methods. * All endpoints accept data via `application/json` request bodies. The API does not accept any data via query params. * The naming convention for endpoints is: localhost:8000/{VERSION}/{METHOD_FAMILY}/{METHOD_NAME} e.g. `localhost:8000/v1/connections/create`. * For all `update` methods, the whole object must be passed in, even the fields that did not change.  Change Management: * The major version of the API endpoint can be determined / specified in the URL `localhost:8080/v1/connections/create` * Minor version bumps will be invisible to the end user. The user cannot specify minor versions in requests. * All backwards incompatible changes will happen in major version bumps. We will not make backwards incompatible changes in minor version bumps. Examples of non-breaking changes (includes but not limited to...):   * Adding fields to request or response bodies.   * Adding new HTTP endpoints. * All `web_backend` APIs are not considered public APIs and are not guaranteeing backwards compatibility. 
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: contact@airbyte.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
import {
    JobConfigType,
    JobConfigTypeFromJSON,
    JobConfigTypeFromJSONTyped,
    JobConfigTypeToJSON,
} from './JobConfigType';
import {
    LogRead,
    LogReadFromJSON,
    LogReadFromJSONTyped,
    LogReadToJSON,
} from './LogRead';

/**
 * 
 * @export
 * @interface SynchronousJobRead
 */
export interface SynchronousJobRead {
    /**
     * 
     * @type {string}
     * @memberof SynchronousJobRead
     */
    id: string;
    /**
     * 
     * @type {JobConfigType}
     * @memberof SynchronousJobRead
     */
    configType: JobConfigType;
    /**
     * only present if a config id was provided.
     * @type {string}
     * @memberof SynchronousJobRead
     */
    configId?: string;
    /**
     * 
     * @type {number}
     * @memberof SynchronousJobRead
     */
    createdAt: number;
    /**
     * 
     * @type {number}
     * @memberof SynchronousJobRead
     */
    endedAt: number;
    /**
     * 
     * @type {boolean}
     * @memberof SynchronousJobRead
     */
    succeeded: boolean;
    /**
     * 
     * @type {LogRead}
     * @memberof SynchronousJobRead
     */
    logs?: LogRead;
}

export function SynchronousJobReadFromJSON(json: any): SynchronousJobRead {
    return SynchronousJobReadFromJSONTyped(json, false);
}

export function SynchronousJobReadFromJSONTyped(json: any, ignoreDiscriminator: boolean): SynchronousJobRead {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'id': json['id'],
        'configType': JobConfigTypeFromJSON(json['configType']),
        'configId': !exists(json, 'configId') ? undefined : json['configId'],
        'createdAt': json['createdAt'],
        'endedAt': json['endedAt'],
        'succeeded': json['succeeded'],
        'logs': !exists(json, 'logs') ? undefined : LogReadFromJSON(json['logs']),
    };
}

export function SynchronousJobReadToJSON(value?: SynchronousJobRead | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'id': value.id,
        'configType': JobConfigTypeToJSON(value.configType),
        'configId': value.configId,
        'createdAt': value.createdAt,
        'endedAt': value.endedAt,
        'succeeded': value.succeeded,
        'logs': LogReadToJSON(value.logs),
    };
}
