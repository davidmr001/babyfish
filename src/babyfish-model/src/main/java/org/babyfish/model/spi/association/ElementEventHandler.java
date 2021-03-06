/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.model.spi.association;

import java.util.Collection;
import java.util.Map;

import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.modification.SetModifications.ResumeViaFrozenContext;
import org.babyfish.collection.event.modification.SetModifications.SuspendViaFrozenContext;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.model.spi.reference.Reference;

/**
 * @author Tao Chen
 */
abstract class ElementEventHandler<E> extends AbandonableEventHandler<E> {
    
    /*
     * Choose validate in "modifying" event so that the end-point validation 
     * of both two sides can be executed before real data changing.
     */
    @SuppressWarnings("unchecked")
    public void preHandle(ElementEvent<E> e) {
        
        AssociatedEndpoint endpoint = this.getEndpoint();
        if (endpoint.getAssociationProperty().getOppositeProperty() == null) {
             return;
        }
        
        if (!endpoint.isSuspended()) {
            this.setSuspended(true);
            try {
                E detachedElement = null;
                if (!(e.getFinalModification() instanceof SuspendViaFrozenContext<?>)) {
                    detachedElement = this.loaded(e.getElement(PropertyVersion.DETACH));
                }
                E attachedElement = null;
                if (!(e.getFinalModification() instanceof ResumeViaFrozenContext<?>)) {
                    attachedElement = this.loaded(e.getElement(PropertyVersion.ATTACH));
                }
                if (detachedElement == attachedElement) {
                    return;
                }
                if (attachedElement != null) {
                    Object owner = endpoint.getOwner();
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(attachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (oppositeEndpoint instanceof Reference<?>) {
                            Reference<Object> reference = (Reference<Object>)oppositeEndpoint;
                            reference.validate(owner);
                        } else if (oppositeEndpoint instanceof Collection<?>) {
                            XCollection<Object> collection = (XCollection<Object>)oppositeEndpoint;
                            collection.validate(owner);
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
    
    /*
     * Choose to modify opposite end-point in the "modified" event, becasue
     * 
     * If do the modification for opposite end-point in "modifying", such as:
     *      parent1.getChildNodes().addAll(parent2.getChildNodes());
     * The java.util.ConconrrentModificationException will be raised.
     */
    @SuppressWarnings("unchecked")
    public void postHandle(ElementEvent<E> e) {
        
        AssociatedEndpoint endpoint = this.getEndpoint();
        if (endpoint.getAssociationProperty().getOppositeProperty() == null) {
             return;
        }
        
        if (!endpoint.isSuspended() && e.isModificationSuccessed()) {
            this.setSuspended(true);
            try {
                E detachedElement = null;
                if (!(e.getFinalModification() instanceof SuspendViaFrozenContext<?>)) {
                    detachedElement = this.loaded(e.getElement(PropertyVersion.DETACH));
                }
                E attachedElement = null;
                if (!(e.getFinalModification() instanceof ResumeViaFrozenContext<?>)) {
                    attachedElement = this.loaded(e.getElement(PropertyVersion.ATTACH));
                }
                if (detachedElement == attachedElement) {
                    return;
                }
                Object owner = endpoint.getOwner();
                if (detachedElement != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(detachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                        try {
                            if (oppositeEndpoint instanceof Reference<?>) {
                                Reference<?> reference = (Reference<?>)oppositeEndpoint;
                                reference.set(null);
                            } else if (oppositeEndpoint instanceof Collection<?>) {
                                Collection<?> collection = (Collection<?>)oppositeEndpoint;
                                collection.remove(owner);
                            } else if (oppositeEndpoint instanceof Map<?, ?>) {
                                Map<?, ?> map = (Map<?, ?>)oppositeEndpoint;
                                map.values().remove(owner);
                            } else {
                                throw new AssertionError();
                            }
                        } finally {
                            if (isOppositeEndpointDisabled) {
                                oppositeEndpoint.disable();
                            }
                        }
                    }
                }
                if (attachedElement != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(attachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                        try {
                            if (oppositeEndpoint instanceof Reference<?>) {
                                Reference<Object> reference = (Reference<Object>)oppositeEndpoint;
                                reference.set(owner);
                            } else if (oppositeEndpoint instanceof Collection<?>) {
                                Collection<Object> collection = (Collection<Object>)oppositeEndpoint;
                                collection.add(owner);
                            } else {
                                throw new AssertionError();
                            }
                        } finally {
                            if (isOppositeEndpointDisabled) {
                                oppositeEndpoint.disable();
                            }
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
}
