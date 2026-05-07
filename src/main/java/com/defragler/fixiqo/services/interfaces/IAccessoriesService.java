package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;

import java.util.*;

/**
 * Service responsible for managing accessories assigned to repair requests.
 *
 * <p>Provides high-level operations for:
 * <ul>
 *     <li>Adding accessories to request</li>
 *     <li>Replacing accessories</li>
 *     <li>Retrieving accessories by request</li>
 * </ul></p>
 */
public interface IAccessoriesService {

    List<Accessories> getByRequestId(long requestId);

    void addAccessories(long requestId, List<Integer> accessoriesTypeIds);

    void replaceAccessories(long requestId, List<Integer> accessoriesTypeIds);

    void removeAll(long requestId);
}